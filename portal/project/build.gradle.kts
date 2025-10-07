plugins {
    id("org.eazyportal.plugin.gradle.integration-test-conventions")
    id("org.eazyportal.plugin.gradle.kotlin-lib-conventions")
    id("org.eazyportal.plugin.gradle.repositories-conventions")

    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("eazy-portal-project") {
            id = "${project.group}.portal.project"
            implementationClass = "org.eazyportal.plugin.gradle.portal.EazyPortalProjectPlugin"
            tags = listOf("gradle", "eazy-portal", "project")
        }
    }
}
