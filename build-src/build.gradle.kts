plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.owasp:dependency-check-gradle:latest.release")
    implementation("com.netflix.nebula:gradle-contacts-plugin:6.0.0")
    implementation("com.netflix.nebula:gradle-info-plugin:11.3.3")
    implementation("com.netflix.nebula:nebula-release-plugin:16.0.0")
    implementation("com.netflix.nebula:nebula-publishing-plugin:18.4.0")
    implementation("com.netflix.nebula:nebula-project-plugin:9.6.3")
    implementation("io.github.gradle-nexus:publish-plugin:1.0.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain {
        this as JavaToolchainSpec
        languageVersion.set(JavaLanguageVersion.of("17"))
    }
}
