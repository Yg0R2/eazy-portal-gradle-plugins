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
            implementationClass = "org.eazyportal.plugin.gradle.portal.EazyPortalSettingsPlugin"
            tags = listOf("gradle", "eazy-portal", "settings")
        }
    }
}
