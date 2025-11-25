plugins {
    id("gradle-plugins-conventions")

    `java-gradle-plugin`
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        create("release") {
            id = "${project.group}.${project.name}"
            implementationClass = "org.eazyportal.plugin.gradle.release.EazyReleasePlugin"
            tags = listOf("eazy-portal", "gradle", "release")
        }
    }
}

dependencies {
    // dependencies
    implementation(gradleApi())
    implementation(project(":release-core"))

    // TestFixtures dependencies
    testFixturesApi(testFixtures(project(":release-core")))
}
