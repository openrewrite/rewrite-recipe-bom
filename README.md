<p align="center">
  <a href="https://docs.openrewrite.org">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss-dark.svg">
      <source media="(prefers-color-scheme: light)" srcset="https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss-light.svg">
      <img alt="OpenRewrite Logo" src="https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss-light.svg" width='600px'>
    </picture>
  </a>
</p>

<div align="center">
  <h1>rewrite-recipe-bom</h1>
</div>

<div align="center">

<!-- Keep the gap above this line, otherwise they won't render correctly! -->
[![ci](https://codegenomeproject.org/ci/modules/openrewrite/rewrite-recipe-bom/badge.svg)](https://codegenomeproject.org/ci/modules/openrewrite/rewrite-recipe-bom/builds/latest)
[![Apache 2.0](https://img.shields.io/github/license/openrewrite/rewrite-recipe-bom.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Contributing Guide](https://img.shields.io/badge/Contributing-Guide-informational)](https://github.com/openrewrite/.github/blob/main/CONTRIBUTING.md)
</div>

## What is this?

Align version numbers for all OpenRewrite maintained recipe modules.
Intended to help with developing Recipe modules which take dependencies or otherwise build upon rewrite-maintained Recipe modules.

## What's included

The BOM manages the modules from the repositories below, where each arrow points from a repository to the ones that depend on it.
The [published page](https://openrewrite.github.io/rewrite-recipe-bom/) also lists the order to build them in from source.

[![Dependencies between the repositories behind rewrite-recipe-bom](https://openrewrite.github.io/rewrite-recipe-bom/dependencies.svg)](https://openrewrite.github.io/rewrite-recipe-bom/)

## Where to get it

Releases after 3.38.0 are published to the [Code Genome Project](https://artifacts.codegenomeproject.org/maven) rather than Maven Central, and that repository requires authentication.
To get access, sign in to the Code Genome Project and create a download token; your build then authenticates with the email or username you signed in with, plus that token as the password.
What you can download depends on your account: the Apache-licensed modules are available to any authenticated user, while [source-available](https://docs.openrewrite.org/licensing/openrewrite-licensing) modules require a Moderne subscription.

Keep Maven Central in your build alongside the Code Genome Project, which hosts only OpenRewrite and Moderne artifacts, so the third-party libraries these modules depend on still resolve from Maven Central.
See [the quickstart guide](https://docs.openrewrite.org/running-recipes/getting-started#step-2-add-rewrite-maven-plugin-or-rewrite-gradle-plugin-to-your-project) for more details.

## Maven Usage

Maven provides the [Bill of Materials pattern](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#bill-of-materials-bom-poms) for aligning dependency versions.

```xml
<project>
    <repositories>
        <repository>
            <id>codegenome</id>
            <url>https://artifacts.codegenomeproject.org/maven</url>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>org.openrewrite.recipe</groupId>
            <artifactId>rewrite-logging-frameworks</artifactId>
        </dependency>
        <dependency>
            <groupId>org.openrewrite.recipe</groupId>
            <artifactId>rewrite-testing-frameworks</artifactId>
        </dependency>
    </dependencies>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.openrewrite.recipe</groupId>
                <artifactId>rewrite-recipe-bom</artifactId>
                <version><!-- desired version here --></version>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

Add your credentials to your Maven `settings.xml` (typically at `~/.m2/settings.xml`):

```xml
<settings>
    <servers>
        <server>
            <id>codegenome</id>
            <username>USERNAME</username>
            <password>TOKEN</password>
        </server>
    </servers>
</settings>
```


## Gradle Usage

Gradle provides the [platform](https://docs.gradle.org/current/userguide/platforms.html#sub:using-platform-to-control-transitive-deps) function for aligning dependency versions.

```groovy
repositories {
    mavenCentral()
    maven {
        url = "https://artifacts.codegenomeproject.org/maven"
        credentials {
            username = providers.gradleProperty("codegenomeUsername").get()
            password = providers.gradleProperty("codegenomePassword").get()
        }
    }
}

dependencies {
    implementation(platform("org.openrewrite.recipe:rewrite-recipe-bom:<version>"))
    
    // No need to specify version numbers
    implementation("org.openrewrite.recipe:rewrite-logging-frameworks")
    implementation("org.openrewrite.recipe:rewrite-testing-frameworks")
}
```

Add your credentials to `~/.gradle/gradle.properties` rather than the copy in your repository, so they are never committed:

```properties
codegenomeUsername=you@example.com
codegenomePassword=your-download-token
```

In CI, set the `ORG_GRADLE_PROJECT_codegenomeUsername` and `ORG_GRADLE_PROJECT_codegenomePassword` environment variables instead.
