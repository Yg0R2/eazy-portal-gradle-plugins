package org.eazyportal.plugin.gradle.portal.project

import org.eazyportal.plugin.common.integration.test.testcase.BaseProjectTestCase
import org.junit.jupiter.api.Test

class ApplyEazyPortalProjectPluginIntegrationTest : BaseProjectTestCase() {

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
