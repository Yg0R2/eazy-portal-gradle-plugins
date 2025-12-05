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

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    // dependencies
    implementation(gradleApi())
    implementation(project(":release-core"))

    // TestFixtures dependencies
    testFixturesApi(testFixtures(project(":release-core")))

    // IntegrationTest dependencies
    integrationTestImplementation("org.reflections:reflections:0.10.2")
}
