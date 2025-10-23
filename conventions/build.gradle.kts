plugins {
    `kotlin-dsl`
}

apply(from = "./src/main/kotlin/org.eazyportal.plugin.gradle.conventions.publish-conventions.gradle.kts")

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:${embeddedKotlinVersion}")
}
