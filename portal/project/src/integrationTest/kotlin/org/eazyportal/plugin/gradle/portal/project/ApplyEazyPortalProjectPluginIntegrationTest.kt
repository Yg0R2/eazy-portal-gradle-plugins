package org.eazyportal.plugin.gradle.portal.project

import org.eazyportal.plugin.common.integration.test.testcase.BaseGradleProjectTestCase
import org.junit.jupiter.api.Test

class ApplyEazyPortalProjectPluginIntegrationTest : BaseGradleProjectTestCase() {

    @Test
    fun test_applyPlugin() = runTestCase {
        givenTestCase {
            withEazyPortalProjectPlugin()
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
