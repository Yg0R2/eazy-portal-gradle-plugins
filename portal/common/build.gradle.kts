plugins {
    id("gradle-plugins-conventions")
}

dependencies {
    implementation(gradleApi())

    testFixturesImplementation(gradleTestKit())
}
