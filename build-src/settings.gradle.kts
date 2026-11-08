apply(from = "../gradle/maven-central-mirror.settings.gradle.kts")

pluginManagement.repositories {
    gradlePluginPortal()
}

dependencyResolutionManagement.repositories {
    mavenCentral()
    gradlePluginPortal()
}
