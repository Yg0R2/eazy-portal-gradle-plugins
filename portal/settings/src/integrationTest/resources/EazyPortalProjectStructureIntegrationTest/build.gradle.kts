allprojects {
    dependencies {
        testImplementation(platform("org.junit:junit-bom:+"))

        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }
}
