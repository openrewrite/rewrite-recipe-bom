import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Element
import java.time.LocalDate
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilderFactory

/// Writes the dependencies between the GitHub repositories behind the modules in this BOM as a Graphviz
/// `dependencies.dot`, ranked by build round, next to an `index.html` that lists the build order.
abstract class DependencyGraph : DefaultTask() {

    companion object {
        private val GITHUB_REPOSITORY = Regex("github\\.com/([^/]+/[^/.]+)")

        /// The Gradle plugin publication names no GitHub repository in its POM
        private val REPOSITORY_OVERRIDES = mapOf("org.openrewrite:plugin" to "openrewrite/rewrite-gradle-plugin")

        /// Build time dependencies that no published POM expresses
        private val ADDITIONAL_DEPENDENCIES = mapOf(
            "openrewrite/rewrite-gradle-plugin" to setOf("openrewrite/rewrite-polyglot"),
            "openrewrite/rewrite-templating" to setOf("openrewrite/rewrite"),
        )
    }

    @get:Inject
    abstract val configurations: ConfigurationContainer

    @get:Inject
    abstract val dependencyHandler: DependencyHandler

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    private val documentBuilderFactory = DocumentBuilderFactory.newInstance()

    @TaskAction
    fun generate() {
        val api = configurations.getByName("api").dependencies.map { "${it.group}:${it.name}:${it.version}" }
        val components = detachedConfiguration(api).incoming.resolutionResult.allComponents
            .filter { tracked(it) }
        val repositoryByModule = repositories(components)
        val bomRepositories = api.map { repositoryByModule.getValue(it.substringBeforeLast(':')) }.toSet()

        val graph = bomRepositories.associateWithTo(sortedMapOf()) { sortedSetOf<String>() }
        for (component in components) {
            val repository = repositoryByModule.getValue(module(component))
            graph[repository]?.addAll(component.dependencies
                .filterIsInstance<ResolvedDependencyResult>()
                .filterNot { it.isConstraint }
                .map { it.selected }
                .filter { tracked(it) }
                .map { repositoryByModule.getValue(module(it)) }
                .filter { it != repository && it in bomRepositories })
        }
        ADDITIONAL_DEPENDENCIES.forEach { (repository, dependencies) -> graph[repository]?.addAll(dependencies) }
        removeTransitiveDependencies(graph)

        val rounds = rounds(graph)
        val directory = outputDirectory.get().asFile
        directory.resolve("dependencies.dot").writeText(dot(graph, rounds))
        directory.resolve("index.html").writeText(html(rounds))
        logger.lifecycle("Wrote ${graph.size} repositories in ${rounds.size} build rounds to $directory")
    }

    /// Keeps only the nearest dependencies, so the graph stays legible; the build rounds are the same either way.
    private fun removeTransitiveDependencies(graph: Map<String, MutableSet<String>>) {
        val reachable = mutableMapOf<String, Set<String>>()
        fun reachableFrom(repository: String): Set<String> = reachable.getOrPut(repository) {
            graph.getValue(repository).flatMapTo(mutableSetOf()) { setOf(it) + reachableFrom(it) }
        }
        graph.values.forEach { dependencies ->
            dependencies.removeAll(dependencies.flatMapTo(mutableSetOf()) { reachableFrom(it) })
        }
    }

    private fun rounds(graph: Map<String, Set<String>>): List<List<String>> {
        val rounds = mutableMapOf<String, Int>()
        fun round(repository: String, path: List<String> = listOf()): Int = rounds.getOrPut(repository) {
            if (repository in path) {
                throw GradleException("Dependency cycle: ${(path + repository).joinToString(" -> ")}")
            }
            graph.getValue(repository).maxOfOrNull { round(it, path + repository) + 1 } ?: 0
        }
        return graph.keys.groupBy { round(it) }.toSortedMap().values.toList()
    }

    private fun dot(graph: Map<String, Set<String>>, rounds: List<List<String>>) = buildString {
        appendLine("digraph dependencies {")
        appendLine("  rankdir=LR; ranksep=1; nodesep=0.15;")
        appendLine("  node [shape=box, style=\"rounded,filled\", fillcolor=white, fontname=Helvetica, fontsize=11];")
        appendLine("  edge [color=\"#9a9a9a\", arrowsize=0.6];")
        rounds.forEach { round ->
            appendLine("  { rank=same; ${round.joinToString(" ") { "\"$it\";" }} }")
        }
        graph.keys.forEach {
            appendLine("  \"$it\" [label=\"${it.substringAfter('/')}\", URL=\"https://github.com/$it\", target=\"_top\"];")
        }
        graph.forEach { (repository, dependencies) ->
            dependencies.forEach { appendLine("  \"$it\" -> \"$repository\";") }
        }
        appendLine("}")
    }

    private fun html(rounds: List<List<String>>) = """
        |<!doctype html>
        |<html lang="en">
        |<head>
        |<meta charset="utf-8">
        |<title>rewrite-recipe-bom build order</title>
        |<style>
        |body { font-family: system-ui, sans-serif; margin: 2rem; line-height: 1.5; }
        |a { color: inherit; }
        |object { max-width: 100%; }
        |</style>
        |</head>
        |<body>
        |<h1>rewrite-recipe-bom build order</h1>
        |<p>How the repositories behind <a href="https://github.com/openrewrite/rewrite-recipe-bom">rewrite-recipe-bom</a>
        |depend on one another, as of ${LocalDate.now()}. An arrow points from a repository to the ones that depend on it.</p>
        |<object type="image/svg+xml" data="dependencies.svg">dependencies.svg</object>
        |<h2>Build order</h2>
        |<p>Each round only depends on earlier rounds, so the repositories within a round can be built in any order.</p>
        |<ol start="0">
        |${rounds.joinToString("\n") { round -> "<li>${round.joinToString(", ") { "<a href=\"https://github.com/$it\">${it.substringAfter('/')}</a>" }}</li>" }}
        |</ol>
        |</body>
        |</html>
        |""".trimMargin()

    /// Reads every POM in a single resolution, rather than one round trip per module
    private fun repositories(components: Collection<ResolvedComponentResult>): Map<String, String> {
        val unmapped = components.map { "${module(it)}:${it.moduleVersion!!.version}" }
            .filterNot { it.substringBeforeLast(':') in REPOSITORY_OVERRIDES }
        val poms = detachedConfiguration(unmapped.map { "$it@pom" })
            .apply { isTransitive = false }
            .incoming.artifactView { isLenient = true }.artifacts
            .associate { artifact ->
                val id = artifact.id.componentIdentifier as ModuleComponentIdentifier
                "${id.group}:${id.module}" to documentBuilderFactory.newDocumentBuilder().parse(artifact.file).documentElement
            }
        return REPOSITORY_OVERRIDES + unmapped.map { it.substringBeforeLast(':') }.associateWith { module ->
            val pom = poms[module] ?: throw GradleException("Could not read the POM of $module")
            githubRepository(pom) ?: throw GradleException(
                "The POM of $module names no GitHub repository; add it to REPOSITORY_OVERRIDES")
        }
    }

    private fun githubRepository(pom: Element): String? =
        listOfNotNull(text(pom, "url"), child(pom, "scm")?.let { text(it, "url") })
            .firstNotNullOfOrNull { GITHUB_REPOSITORY.find(it)?.groupValues?.get(1) }

    private fun module(component: ResolvedComponentResult) =
        component.moduleVersion!!.let { "${it.group}:${it.name}" }

    private fun tracked(component: ResolvedComponentResult) =
        component.moduleVersion?.group?.let { it == "org.openrewrite" || it.startsWith("org.openrewrite.") } == true

    private fun detachedConfiguration(modules: Collection<String>): Configuration = configurations
        .detachedConfiguration(*modules.map { dependencyHandler.create(it) }.toTypedArray())

    private fun child(parent: Element, tag: String): Element? =
        (0 until parent.childNodes.length)
            .map { parent.childNodes.item(it) }
            .filterIsInstance<Element>()
            .firstOrNull { it.tagName == tag }

    private fun text(parent: Element, tag: String): String? = child(parent, tag)?.textContent?.trim()
}
