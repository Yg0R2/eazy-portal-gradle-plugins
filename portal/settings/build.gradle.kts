plugins {
    id("org.eazyportal.plugin.gradle.integration-test-conventions")
    id("org.eazyportal.plugin.gradle.kotlin-lib-conventions")
    id("org.eazyportal.plugin.gradle.repositories-conventions")

    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("eazy-portal-settings") {
            id = "${project.group}.portal.settings"
            implementationClass = "org.eazyportal.plugin.gradle.portal.settings.EazyPortalSettingsPlugin"
            tags = listOf("eazy-portal", "gradle", "settings")
        }
    }
}

dependencies {
    implementation(project(":portal-common"))
    implementation(project(":portal-project"))
}
