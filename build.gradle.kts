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

repositories {
    gradlePluginPortal()
}

val latest = if (project.hasProperty("releasing")) "latest.release" else "latest.integration"
dependencies {
    api(platform("org.openrewrite:rewrite-bom:$latest"))

    api("org.openrewrite:plugin:$latest")
    api("org.openrewrite.maven:rewrite-maven-plugin:$latest")

    api("org.openrewrite:rewrite-csharp:$latest")
    api("org.openrewrite:rewrite-javascript:$latest")
    api("org.openrewrite:rewrite-polyglot:$latest")
    api("org.openrewrite:rewrite-python:$latest")
    api("org.openrewrite:rewrite-templating:$latest")

    api("org.openrewrite.meta:rewrite-analysis:$latest")

    api("org.openrewrite.recipe:rewrite-all:$latest")
    api("org.openrewrite.recipe:rewrite-apache:$latest")
    api("org.openrewrite.recipe:rewrite-codemods:$latest")
    api("org.openrewrite.recipe:rewrite-cucumber-jvm:$latest")
    api("org.openrewrite.recipe:rewrite-docker:$latest")
    api("org.openrewrite.recipe:rewrite-dropwizard:$latest")
    api("org.openrewrite.recipe:rewrite-feature-flags:$latest")
    api("org.openrewrite.recipe:rewrite-github-actions:$latest")
    api("org.openrewrite.recipe:rewrite-gitlab:$latest")
    api("org.openrewrite.recipe:rewrite-hibernate:$latest")
    api("org.openrewrite.recipe:rewrite-jackson:$latest")
    api("org.openrewrite.recipe:rewrite-java-dependencies:$latest")
    api("org.openrewrite.recipe:rewrite-jenkins:$latest")
    api("org.openrewrite.recipe:rewrite-joda:$latest")
    api("org.openrewrite.recipe:rewrite-liberty:$latest")
    api("org.openrewrite.recipe:rewrite-logging-frameworks:$latest")
    api("org.openrewrite.recipe:rewrite-micrometer:$latest")
    api("org.openrewrite.recipe:rewrite-micronaut:$latest")
    api("org.openrewrite.recipe:rewrite-migrate-java:$latest")
    api("org.openrewrite.recipe:rewrite-netty:$latest")
    api("org.openrewrite.recipe:rewrite-okhttp:$latest")
    api("org.openrewrite.recipe:rewrite-openapi:$latest")
    api("org.openrewrite.recipe:rewrite-quarkus:$latest")
    api("org.openrewrite.recipe:rewrite-rewrite:$latest")
    api("org.openrewrite.recipe:rewrite-spring:$latest")
    api("org.openrewrite.recipe:rewrite-spring-to-quarkus:$latest")
    api("org.openrewrite.recipe:rewrite-static-analysis:$latest")
    api("org.openrewrite.recipe:rewrite-struts:$latest")
    api("org.openrewrite.recipe:rewrite-testing-frameworks:$latest")
    api("org.openrewrite.recipe:rewrite-third-party:$latest")
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

tasks.register("licenseFormat") {
    println("License format task not implemented")
}
