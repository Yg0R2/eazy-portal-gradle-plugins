package org.eazyportal.plugin.gradle.portal.settings

import org.eazyportal.plugin.common.integration.test.testcase.BaseGradleProjectTestCase
import org.eazyportal.plugin.gradle.portal.project.EazyPortalProjectPlugin
import org.junit.jupiter.api.Test

class ApplyEazyPortalSettingsPluginIntegrationTest : BaseGradleProjectTestCase() {

    @Test
    fun test_applyPlugin() = runTestCase {
        givenTestCase {
            withGradleProject {
                withEazyPortalSettingsPlugin()
                withListPluginsTask()
            }
        }

        whenExecute {
            taskSucceeds("listPlugins")
        }

        thenVerify {
            taskOutput {
                contains(EazyPortalProjectPlugin::class.java.name)
            }
        }
    }

}
