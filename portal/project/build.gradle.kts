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
            implementationClass = "org.eazyportal.plugin.gradle.portal.project.EazyPortalProjectPlugin"
            tags = listOf("eazy-portal", "gradle", "project")
        }
    }
}

dependencies {
    // dependencies
    implementation(project(":conventions"))
    implementation(project(":portal-common"))

    // Test dependencies
    testImplementation(testFixtures(project(":portal-common")))
}
