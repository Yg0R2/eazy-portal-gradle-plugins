package org.eazyportal.gradle.eazyproject.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FrameworkTest {

    @Test
    fun `SPRING_BOOT renders as its kebab name`() {
        assertThat(Framework.SPRING_BOOT.toString())
            .isEqualTo("spring-boot")
    }

}
