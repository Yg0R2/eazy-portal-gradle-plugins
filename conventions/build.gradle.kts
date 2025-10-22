plugins {
    `kotlin-dsl`
}

group = "org.eazyportal.plugin.gradle"

apply(from = "./src/main/kotlin/org.eazyportal.plugin.gradle.conventions.publish-conventions.gradle.kts")

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.gradle.kotlin.kotlin-dsl:org.gradle.kotlin.kotlin-dsl.gradle.plugin:6.2.0")
}
