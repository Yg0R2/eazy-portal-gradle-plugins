plugins {
    id("gradle-plugins-conventions")
}

dependencies {
    // dependencies
    implementation(gradleApi())

    // TestFixtures dependencies
    testFixturesApi(testFixtures(project(":common")))

    testFixturesImplementation(gradleTestKit())
}
