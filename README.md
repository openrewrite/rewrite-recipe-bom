# rewrite-recipe-bom

Align version numbers for all OpenRewrite maintained recipe modules.
Intended to help with developing Recipe modules which take dependencies or otherwise build upon rewrite-maintained Recipe modules.

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
