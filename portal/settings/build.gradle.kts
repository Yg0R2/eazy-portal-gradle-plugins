plugins {
    id("gradle-plugins-conventions")

    `java-gradle-plugin`
}

group = "org.eazyportal.plugin.gradle.portal"

gradlePlugin {
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
