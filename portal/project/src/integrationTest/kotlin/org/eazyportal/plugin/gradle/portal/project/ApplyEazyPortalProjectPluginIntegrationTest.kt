package org.eazyportal.plugin.gradle.portal.project

import org.eazyportal.plugin.common.integration.test.testcase.BaseGradleProjectTestCase
import org.junit.jupiter.api.Test

class ApplyEazyPortalProjectPluginIntegrationTest : BaseGradleProjectTestCase() {

    @Test
    fun test_applyPlugin() {
        givenTestCase {
            withEazyPortalProjectPlugin()
            withListPluginsTask()
        }.whenGradleTaskSucceeds("listPlugins")
            .thenAssertTaskOutput {
                contains(EazyPortalProjectPlugin::class.java.name)
            }
    }

}
