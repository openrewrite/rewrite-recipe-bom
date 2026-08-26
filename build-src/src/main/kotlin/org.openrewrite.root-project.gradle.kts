plugins {
    id("org.openrewrite.base")
    id("com.netflix.nebula.release")
}

tasks.register("closeAndReleaseSonatypeStagingRepository") {
    group = "publishing"
    description = "No-op stand-in for the task the Nexus plugin used to contribute, which gh-automation's publish-gradle.yml still invokes by name. Artifacts publish to the Code Genome Project, not Maven Central."
}

configure<nebula.plugin.release.git.base.ReleasePluginExtension> {
    defaultVersionStrategy = nebula.plugin.release.NetflixOssStrategies.SNAPSHOT(project)
}
