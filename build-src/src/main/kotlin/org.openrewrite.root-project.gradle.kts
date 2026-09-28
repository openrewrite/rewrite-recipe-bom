plugins {
    id("org.openrewrite.base")
    id("com.netflix.nebula.release")
}

configure<nebula.plugin.release.git.base.ReleasePluginExtension> {
    defaultVersionStrategy = nebula.plugin.release.NetflixOssStrategies.SNAPSHOT(project)
}
