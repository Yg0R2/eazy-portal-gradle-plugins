import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.publish.maven.MavenPublication

/*
 * Hand-written build for the conventions included build — the one build that cannot apply its own
 * conventions (chicken-and-egg). Keep the publishing block in sync with `publication-convention`
 * (design §4b.1); placeholders («…») are resolved in TOOLS-76.
 */
plugins {
    `kotlin-dsl`      // pins Kotlin to embeddedKotlinVersion + enables precompiled script plugins
    `maven-publish`   // the conventions jar must itself be published (§4.1) — cannot dogfood here
    signing           // release artifacts are signed (mirrors publication-convention)
    `java-test-fixtures`
}

description = "EazyPortal Gradle convention plugins"

dependencies {
    // Makes `kotlin("jvm")` applicable from our precompiled scripts, tied to the embedded version:
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$embeddedKotlinVersion")

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(gradleTestKit())

    testRuntimeOnly(libs.junit.platform.launcher)

    testFixturesImplementation(platform(libs.junit.bom))
    testFixturesImplementation(libs.assertj.core)
    testFixturesImplementation(gradleTestKit())
}

// Align the Java toolchain used to COMPILE the plugins to 25 (→ bytecode 25 → requires a JDK 25+ daemon).
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// Belt-and-suspenders: also set the Kotlin jvmTarget explicitly. The ClassFileMajorVersionTest gate
// (design §9.2) asserts class-file major version 69; if kotlin-dsl ever fights this explicit value,
// that gate catches it.
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_25
    }
}

// Minimal release detection — mirrors ConventionsSupport.isRelease (TOOLS-60); proper SemVer
// handling is backlog TOOLS-77.
val release = version.toString().let { it.isNotBlank() && it != "unspecified" && !it.endsWith("-SNAPSHOT") }

publishing {
    repositories {
        // Release → GitHub Packages. SNAPSHOT → no custom repo (routes to mavenLocal, see below).
        if (release) {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/«github-owner»/«github-repo»")
                credentials(PasswordCredentials::class)   // lazy: GitHubPackagesUsername / GitHubPackagesPassword
            }
        }
    }

    // Central POM applied to EVERY publication (`pluginMaven` + plugin markers): license + scm are
    // required, the conventions jar must NOT be published with a bare POM.
    publications.withType<MavenPublication>().configureEach {
        pom {
            name = provider { project.name }
            description = provider { project.description ?: project.name }
            url = "https://github.com/«github-owner»/«github-repo»"
            licenses {
                license {
                    name = "«license-name»"
                    url = "https://raw.githubusercontent.com/«github-owner»/«github-repo»/refs/heads/main/LICENSE"
                }
            }
            scm {
                url = "https://github.com/«github-owner»/«github-repo»"
                connection = "scm:git:https://github.com/«github-owner»/«github-repo».git"
                developerConnection = "scm:git:ssh://git@github.com/«github-owner»/«github-repo».git"
            }
        }
    }
}

// Sign RELEASE artifacts only (SNAPSHOT → mavenLocal stays unsigned). Keys from CI secrets,
// in-memory + ASCII-armored — never on disk.
if (release) {
    signing {
        useInMemoryPgpKeys(
            providers.environmentVariable("SIGNING_KEY").orNull,
            providers.environmentVariable("SIGNING_PASSWORD").orNull,
        )
        sign(publishing.publications)
    }
}

// ONE `publish` command routes by version — SNAPSHOT → mavenLocal, release → GitHub Packages.
// `publishToMavenLocal` keeps working on its own in all cases.
if (!release) {
    tasks.named("publish") { dependsOn(tasks.named("publishToMavenLocal")) }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
