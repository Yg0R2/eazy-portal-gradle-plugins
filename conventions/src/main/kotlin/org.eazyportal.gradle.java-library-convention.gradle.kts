import org.gradle.api.publish.maven.MavenPublication

/*
 * Publishable Java library — design §4.4. Extends java-project (toolchain, -Werror, JUnit platform),
 * layers `java-library` (api/implementation split) and publication-convention (POM + repo routing),
 * then creates the `maven` publication with sources + Javadoc jars.
 */
plugins {
    id("org.eazyportal.gradle.java-project-convention")
    `java-library`
    id("org.eazyportal.gradle.publication-convention")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
