import org.gradle.api.publish.maven.MavenPublication

/*
 * Publishable Kotlin library — design §4.4. Extends kotlin-project (toolchain, -Werror, JUnit platform,
 * Kotlin compile settings), layers `java-library` (api/implementation split) and publication-convention
 * (POM + repo routing), enables `explicitApi()` (strict — public declarations must declare visibility and
 * return type), then creates the `maven` publication with a sources jar only. No Javadoc jar: javac produces
 * no useful output for Kotlin sources; a Dokka jar is backlog.
 */
plugins {
    id("org.eazyportal.gradle.kotlin-project-convention")
    `java-library`
    id("org.eazyportal.gradle.publication-convention")
}

kotlin {
    explicitApi()
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
