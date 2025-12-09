plugins {
    id("gradle-plugins-conventions")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    api(gradleTestKit())

    implementation(platform(libs.findLibrary("assertj-bom").get()))
    implementation(platform(libs.findLibrary("junit-bom").get()))

    compileOnly("org.assertj:assertj-core")
    compileOnly("org.junit.jupiter:junit-jupiter")
}
