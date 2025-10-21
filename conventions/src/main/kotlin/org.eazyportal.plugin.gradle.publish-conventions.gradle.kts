plugins.apply("maven-publish")

configure<PublishingExtension> {
    publications {
        if (isKotlinDslPluginPublishEnabled().not()) {
            create("maven", MavenPublication::class) {
                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()

                from(project.components["java"])
            }
        }

        withType<MavenPublication> {
            suppressAllPomMetadataWarnings()

            versionMapping {
                allVariants {
                    fromResolutionResult()
                }
            }
        }
    }

    repositories {
        if (project.version.toString().matches("^\\d+\\.\\d+\\.\\d+$".toRegex())) {
            TODO("add github packages")
        } else {
            mavenLocal()
        }
    }
}

fun isKotlinDslPluginPublishEnabled(): Boolean =
    extensions.findByType<GradlePluginDevelopmentExtension>()
        ?.isAutomatedPublishing
        ?: false
