package org.eazyportal.gradle.eazyproject

import org.eazyportal.gradle.eazyproject.DefaultVersions.EXAMPLE_CORE_DEFAULT_VERSION
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class EazyProjectExtensionTest {

    @Test
    fun `eazyPortalCoreVersion convention defaults to DefaultVersions EXAMPLE_CORE_DEFAULT_VERSION`() {
        val project = ProjectBuilder.builder().build()

        val extension = project.extensions.create("eazyProject", EazyProjectExtension::class.java)

        assertThat(extension.eazyPortalCoreVersion.get())
            .isEqualTo(EXAMPLE_CORE_DEFAULT_VERSION)
    }

    @Test
    fun `eazyPortalCoreVersion can be overridden`() {
        val project = ProjectBuilder.builder().build()

        val extension = project.extensions.create("eazyProject", EazyProjectExtension::class.java)

        extension.eazyPortalCoreVersion.set("9.9.9")

        assertThat(extension.eazyPortalCoreVersion.get())
            .isEqualTo("9.9.9")
    }

}
