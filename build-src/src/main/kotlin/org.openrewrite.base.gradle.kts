import nebula.plugin.contacts.Contact
import nebula.plugin.contacts.ContactsExtension

plugins {
    base
    id("org.openrewrite.dependency-check")
    id("com.netflix.nebula.contacts")
    id("com.netflix.nebula.info")
}

group = "org.openrewrite"
description = "Eliminate tech-debt. Automatically."

repositories {
    if (!project.hasProperty("releasing")) {
        mavenLocal()
        maven {
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
    }
    mavenCentral()
}

configure<ContactsExtension> {
    val j = Contact("team@moderne.io")
    j.moniker("Moderne")

    people["team@moderne.io"] = j
}
