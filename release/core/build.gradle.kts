plugins {
    id("gradle-plugins-conventions")
}

dependencies {
    // dependencies
    compileOnly("org.slf4j:slf4j-api:+")

    // TestFixtures dependencies
    testFixturesApi(testFixtures(project(":common-integration-test")))

    // Test dependencies
    testImplementation("org.slf4j:slf4j-api:+")

    // IntegrationTest dependencies
    integrationTestImplementation(gradleTestKit())
}
