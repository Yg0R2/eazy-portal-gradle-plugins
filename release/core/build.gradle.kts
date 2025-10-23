plugins {
    id("gradle-plugins-conventions")
}

dependencies {
    // dependencies
    compileOnly("org.slf4j:slf4j-api:+")

    // Test dependencies
    testImplementation("org.slf4j:slf4j-api:+")

    // IntegrationTest dependencies
    integrationTestImplementation(gradleTestKit())
}
