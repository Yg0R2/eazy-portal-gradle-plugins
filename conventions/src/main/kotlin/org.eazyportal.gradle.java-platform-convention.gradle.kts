import org.gradle.api.publish.maven.MavenPublication

plugins {
    `java-platform`
    id("org.eazyportal.gradle.publication-convention")
}

javaPlatform {
    allowDependencies()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["javaPlatform"])
        }
    }
}
