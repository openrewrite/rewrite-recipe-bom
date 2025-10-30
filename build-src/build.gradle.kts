plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.owasp:dependency-check-gradle:latest.release")
    implementation("com.netflix.nebula.contacts:com.netflix.nebula.contacts.gradle.plugin:latest.release")
    implementation("com.netflix.nebula.info:com.netflix.nebula.info.gradle.plugin:latest.release")
    implementation("com.netflix.nebula.release:com.netflix.nebula.release.gradle.plugin:latest.release")
    implementation("com.netflix.nebula:nebula-publishing-plugin:latest.release")
    implementation("com.netflix.nebula:nebula-project-plugin:latest.release")
    implementation("io.github.gradle-nexus:publish-plugin:latest.release")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain {
        this as JavaToolchainSpec
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
