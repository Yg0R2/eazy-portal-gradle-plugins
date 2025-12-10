package org.eazyportal.plugin.gradle.portal.settings

import org.eazyportal.plugin.common.integration.test.testcase.BaseGradleProjectTestCase
import org.eazyportal.plugin.gradle.portal.project.EazyPortalProjectPlugin
import org.junit.jupiter.api.Test

class ApplyEazyPortalSettingsPluginIntegrationTest : BaseGradleProjectTestCase() {

    @Test
    fun test_applyPlugin() = runTestCase {
        givenTestCase {
            withEazyPortalSettingsPlugin()
            withListPluginsTask()
        }
        whenExecute {
            taskSucceeds("listPlugins")
        }
        thenValidate {
            taskOutput {
                contains(EazyPortalProjectPlugin::class.java.name)
            }
        }
    }

}
