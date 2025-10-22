plugins {
    id("gradle-plugins-conventions")
}

group = "org.eazyportal.plugin.gradle.portal"

dependencies {
    implementation(gradleApi())

    testFixturesImplementation(gradleTestKit())
}
