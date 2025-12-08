plugins {
    id("gradle-plugins-conventions")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    api(gradleTestKit())

    implementation(platform(libs.findLibrary("assertj-bom").get()))
//    implementation(platform(libs.findLibrary("junit-bom").get()))

    implementation("org.assertj:assertj-core")
//    implementation("org.junit.jupiter:junit-jupiter")
}
