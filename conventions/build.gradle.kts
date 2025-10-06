plugins {
    `kotlin-dsl`
}

apply(from = "./src/main/kotlin/org.eazyportal.plugin.gradle.publish-conventions.gradle.kts")

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kotlin.dsl)
}
