plugins {
    `kotlin-dsl`
}

apply(from = "./src/main/kotlin/org.eazyportal.plugin.gradle.publish-conventions.gradle.kts")

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.gradle.kotlin.kotlin-dsl:org.gradle.kotlin.kotlin-dsl.gradle.plugin:6.2.0")
}
