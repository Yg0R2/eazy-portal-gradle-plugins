import org.eazyportal.gradle.conventions.ConventionsUtils
import org.gradle.api.publish.maven.MavenPublication

/*
 * Publication routing + POM metadata — design §4b.1. Centralizes WHERE a publication goes
 * (SNAPSHOT -> mavenLocal / release -> GitHub Packages) and the shared POM metadata, but creates
 * NO publications itself — reused unchanged by the library conventions (TOOLS-63) and by
 * gradle-plugin-convention (TOOLS-64).
 */
plugins {
    `maven-publish`
    signing
}

val release = ConventionsUtils.isRelease(version.toString())

publishing {
    repositories {
        // Release -> GitHub Packages. SNAPSHOT -> no custom repo (routes to mavenLocal, see below).
        if (release) {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/«github-owner»/«github-repo»")
                credentials(PasswordCredentials::class)   // lazy: GitHubPackagesUsername / GitHubPackagesPassword
            }
        }
    }

    // Central POM applied to EVERY publication (library `maven` publications + plugin markers).
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

// Sign RELEASE artifacts only (SNAPSHOT -> mavenLocal stays unsigned). Keys from CI secrets,
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

// ONE `publish` command routes by version — SNAPSHOT -> mavenLocal, release -> GitHub Packages.
// `publishToMavenLocal` keeps working on its own in all cases (built-in task, unaffected by the routing).
if (!release) {
    tasks.named("publish") { dependsOn(tasks.named("publishToMavenLocal")) }
}
