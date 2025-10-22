plugins {
    id("gradle-plugins-conventions")

    `java-gradle-plugin`
    `kotlin-dsl`
}

group = "org.eazyportal.plugin.gradle.portal"

gradlePlugin {
    isAutomatedPublishing = false

    plugins {
        create("eazy-portal-settings") {
            id = "${project.group}.${project.name}"
            implementationClass = "org.eazyportal.plugin.gradle.portal.settings.EazyPortalSettingsPlugin"
            tags = listOf("eazy-portal", "gradle", "settings")
        }
    }
}

dependencies {
    // dependencies
    implementation(project(":common"))
    implementation(project(":project"))

    // Test dependencies
    testImplementation(testFixtures(project(":common")))
}
