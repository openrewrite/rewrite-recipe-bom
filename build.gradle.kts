plugins {
    `java-platform`
    id("org.openrewrite.root-project")
    id("org.openrewrite.maven-publish")
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
    resolutionStrategy.cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
}

javaPlatform {
    allowDependencies()
}

group = "org.openrewrite.recipe"

val latest = if (project.hasProperty("releasing")) "latest.release" else "latest.integration"

configurations {
    all {
        resolutionStrategy.eachDependency {
            if (requested.group == "com.fasterxml.jackson.core" && requested.version == "2.13.4") {
                useVersion("2.13.4.2")
                because("CVE-2022-42003 - https://nvd.nist.gov/vuln/detail/CVE-2022-42003")
            }
        }
    }
}

dependencies {
    api(platform("org.openrewrite:rewrite-bom:$latest"))
    api("org.openrewrite.recipe:rewrite-circleci:$latest")
    api("org.openrewrite.recipe:rewrite-cloud-suitability-analyzer:$latest")
    api("org.openrewrite.recipe:rewrite-concourse:$latest")
    api("org.openrewrite.recipe:rewrite-github-actions:$latest")
    api("org.openrewrite.recipe:rewrite-java-security:$latest")
    api("org.openrewrite.recipe:rewrite-jhipster:$latest")
    api("org.openrewrite.recipe:rewrite-kubernetes:$latest")
    api("org.openrewrite.recipe:rewrite-logging-frameworks:$latest")
    api("org.openrewrite.recipe:rewrite-micronaut:$latest")
    api("org.openrewrite.recipe:rewrite-migrate-java:$latest")
    api("org.openrewrite.recipe:rewrite-quarkus:$latest")
    api("org.openrewrite.recipe:rewrite-spring:$latest")
    api("org.openrewrite.recipe:rewrite-terraform:$latest")
    api("org.openrewrite.recipe:rewrite-testing-frameworks:$latest")
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

tasks.register("test") {
    doLast {
        configurations.create("resolveApi") {
            extendsFrom(configurations.getByName("api"))
        }.resolve()
    }
}
