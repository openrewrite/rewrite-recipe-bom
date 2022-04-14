plugins {
    `java-platform`
    id("org.openrewrite.root-project")
    id("org.openrewrite.maven-publish")
}

javaPlatform {
    allowDependencies()
}

val latestStatus = if (project.hasProperty("releasing")) "latest.release" else "latest.integration"

dependencies {
    api(platform("org.openrewrite:rewrite-bom:${latestStatus}"))

    api("org.openrewrite.recipe:rewrite-circleci:${latestStatus}")
    api("org.openrewrite.recipe:rewrite-concourse:${latestStatus}")
    api("org.openrewrite.recipe:rewrite-github-actions:${latestStatus}")
    api("org.openrewrite.recipe:rewrite-java-security:${latestStatus}")
    api("org.openrewrite.recipe:rewrite-jhipster:${latestStatus}")
    api("org.openrewrite.recipe:rewrite-kubernetes:${latestStatus}")
    api("org.openrewrite.recipe:rewrite-logging-frameworks:${latestStatus}")
    api("org.openrewrite.recipe:rewrite-micronaut:${latestStatus}")
    api("org.openrewrite.recipe:rewrite-migrate-java:${latestStatus}")
    api("org.openrewrite.recipe:rewrite-quarkus:${latestStatus}")
    api("org.openrewrite.recipe:rewrite-spring:${latestStatus}")
    api("org.openrewrite.recipe:rewrite-terraform:${latestStatus}")
    api("org.openrewrite.recipe:rewrite-testing-frameworks:${latestStatus}")
}

publishing {
    publications {
        named("nebula", MavenPublication::class.java) {
            from(components["javaPlatform"])

            pom.withXml {
                val root = asElement()
                val dependencyManagement = root.getElementsByTagName("dependencyManagement").item(0) as org.w3c.dom.Element
                val managedDependencies = dependencyManagement.getElementsByTagName("dependencies").item(0) as org.w3c.dom.Element
                val dependencies = root.getElementsByTagName("dependencies").item(1) as org.w3c.dom.Element
                dependencies.getElementsByTagName("dependency").let { dependencyList ->
                    for (i in 0 until dependencyList.length) {
                        val dependency = dependencyList.item(0) as org.w3c.dom.Element
                        dependency.removeChild(dependency.getElementsByTagName("scope").item(0))
                        managedDependencies.appendChild(dependency)
                    }
                }
                root.removeChild(dependencies)
            }
        }
    }
}