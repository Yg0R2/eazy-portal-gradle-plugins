package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.gradle.portal.common.dsl.BaseGradleProjectTestCase
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.RELEASE_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.UPDATE_SCM_TASK_NAME
import org.junit.jupiter.api.Test

class ApplyEazyReleasePluginIntegrationTest : BaseGradleProjectTestCase() {

    @Test
    fun `apply plugin`() = runTestCase {
        givenTestCase {
            withGradleProject {
                withEazyPortalReleasePlugin()
            }
        }

        whenExecute {
            gradleTaskSucceeds("tasks")
        }

        thenVerify {
            gradleTaskOutput {
                contains(
                    "Eazy-release tasks",
                    FINALIZE_RELEASE_VERSION_TASK_NAME,
                    FINALIZE_SNAPSHOT_VERSION_TASK_NAME,
                    PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME,
                    RELEASE_TASK_NAME,
                    SET_RELEASE_VERSION_TASK_NAME,
                    SET_SNAPSHOT_VERSION_TASK_NAME,
                    UPDATE_SCM_TASK_NAME,
                )
            }
        }
    }

    @Test
    fun `apply plugin on subproject should fail`() = runTestCase {
        givenTestCase {
            withGradleProject {
                withSubproject("subproject") {
                    withEazyPortalReleasePlugin()
                }
            }
        }

        whenExecute {
            gradleTaskFails("tasks")
        }

        thenVerify {
            gradleTaskOutput {
                contains(
                    "An exception occurred applying plugin request [id: 'org.eazyportal.plugin.gradle.release-gradle']",
                    "> Failed to apply plugin 'org.eazyportal.plugin.gradle.release-gradle'.",
                    "   > Plugin can be applied only to the root project.",
                )
            }
        }
    }

}
