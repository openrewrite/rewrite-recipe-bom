# rewrite-recipe-bom

Align version numbers for all OpenRewrite maintained recipe modules.
Intended to help with developing Recipe modules which take dependencies or otherwise build upon rewrite-maintained Recipe modules.


<div align="center">

<!-- Keep the gap above this line, otherwise they won't render correctly! -->
[![ci](https://github.com/openrewrite/rewrite-recipe-bom/actions/workflows/ci.yml/badge.svg)](https://github.com/openrewrite/rewrite/actions/workflows/ci.yml)
[![Apache 2.0](https://img.shields.io/github/license/openrewrite/rewrite-recipe-bom.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.openrewrite.recipe/rewrite-recipe-bom.svg)](https://mvnrepository.com/artifact/org.openrewrite.recipe/rewrite-recipe-bom)
[![Contributing Guide](https://img.shields.io/badge/Contributing-Guide-informational)](https://github.com/openrewrite/.github/blob/main/CONTRIBUTING.md)
</div>

## Maven Usage

Maven provides the [Bill of Materials pattern](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#bill-of-materials-bom-poms) for aligning dependency versions.

```xml
<project>
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


## Gradle Usage

Gradle provides the [platform](https://docs.gradle.org/current/userguide/platforms.html#sub:using-platform-to-control-transitive-deps) function for aligning dependency versions.

```groovy
dependencies {
    implementation(platform("org.openrewrite.recipe:rewrite-recipe-bom:<version>"))
    
    // No need to specify version numbers
    implementation("org.openrewrite.recipe:rewrite-logging-frameworks")
    implementation("org.openrewrite.recipe:rewrite-testing-frameworks")
}
```
