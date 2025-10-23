plugins {
    id("gradle-plugins-conventions")

    `java-gradle-plugin`
    `kotlin-dsl`
}

gradlePlugin {
    isAutomatedPublishing = false

    plugins {
        create("eazy-portal-project") {
            id = "${project.group}.${project.name}"
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
