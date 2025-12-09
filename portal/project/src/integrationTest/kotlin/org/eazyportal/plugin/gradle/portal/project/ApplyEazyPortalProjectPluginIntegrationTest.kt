package org.eazyportal.plugin.gradle.portal.project

import org.eazyportal.plugin.common.integration.test.testcase.BaseProjectTestCase
import org.eazyportal.plugin.common.integration.test.testcase.TestCaseBuilder.givenTestCase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ApplyEazyPortalProjectPluginIntegrationTest {

    @Test
    fun test_applyPlugin(@TempDir workingDir: File) {
        givenTestCase<BaseProjectTestCase>(workingDir) {
            withEazyPortalProjectPlugin()
            withListPluginsTask()
        }.whenGradleTaskSucceeds("listPlugins")
            .thenAssertTaskOutput {
                contains(EazyPortalProjectPlugin::class.java.name)
            }
    }

}
