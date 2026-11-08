// What the org.openrewrite.build plugins do for the projects they are applied to, for what they cannot reach: the
// plugin classpath and the build-src included build. Maven Central and the Gradle Plugin Portal go through the mirror
// CI names, or else CGP's Maven cache, because Central answers HTTP 429 once enough builds behind an address use it.
val ciMirrorUrl = providers.environmentVariable("MAVEN_CENTRAL_MIRROR_URL").getOrElse("")
val ciMirrorUsername = providers.environmentVariable("MAVEN_CENTRAL_MIRROR_USERNAME").getOrElse("")
val ciMirrorPassword = providers.environmentVariable("MAVEN_CENTRAL_MIRROR_PASSWORD").getOrElse("")
val ciNamesMirror = ciMirrorUrl.isNotEmpty() && ciMirrorUsername.isNotEmpty() && ciMirrorPassword.isNotEmpty()
val mirrorUrl = if (ciNamesMirror) ciMirrorUrl else "https://artifacts.codegenomeproject.org/maven-public/"
// The portal's own repository beside maven-public, whose version lists are Central's for a plugin both list
val pluginPortalMirrorUrl = mirrorUrl.replace(Regex("/maven-public/?$"), "/gradle-plugins/")
val mirrorUsername = if (ciNamesMirror) ciMirrorUsername else
    providers.gradleProperty("codegenomeUsername").orNull ?: System.getenv("CODEGENOME_USERNAME").orEmpty()
val mirrorPassword = if (ciNamesMirror) ciMirrorPassword else
    providers.gradleProperty("codegenomePassword").orNull ?: System.getenv("CODEGENOME_TOKEN").orEmpty()

fun RepositoryHandler.useMirror() {
    withType<MavenArtifactRepository>().configureEach {
        when (url.host) {
            "repo.maven.apache.org", "repo1.maven.org" -> setUrl(mirrorUrl)
            "plugins.gradle.org" -> setUrl(pluginPortalMirrorUrl)
            else -> return@configureEach
        }
        credentials {
            username = mirrorUsername
            password = mirrorPassword
        }
        mavenContent { releasesOnly() }
    }
}

if (mirrorUsername.isNotEmpty() && mirrorPassword.isNotEmpty()) {
    pluginManagement.repositories.useMirror()
    dependencyResolutionManagement.repositories.useMirror()
}
