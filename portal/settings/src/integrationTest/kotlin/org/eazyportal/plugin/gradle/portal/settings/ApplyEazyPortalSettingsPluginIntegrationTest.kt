package org.eazyportal.plugin.gradle.portal.settings

import org.eazyportal.plugin.common.integration.test.testcase.BaseProjectTestCase
import org.eazyportal.plugin.common.integration.test.testcase.TestCaseBuilder.givenTestCase
import org.eazyportal.plugin.gradle.portal.project.EazyPortalProjectPlugin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ApplyEazyPortalSettingsPluginIntegrationTest {

    @Test
    fun test_applyPlugin(@TempDir workingDir: File) {
        givenTestCase<BaseProjectTestCase>(workingDir) {
            withEazyPortalSettingsPlugin()
            withListPluginsTask()
        }.whenGradleTaskSucceeds("listPlugins")
            .thenAssertTaskOutput {
                contains(EazyPortalProjectPlugin::class.java.name)
            }
    }

}
