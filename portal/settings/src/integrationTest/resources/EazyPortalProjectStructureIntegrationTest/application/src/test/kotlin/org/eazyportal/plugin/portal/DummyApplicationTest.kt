package org.eazyportal.plugin.portal

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class DummyApplicationTest {

    @Test
    fun test() {
        assertDoesNotThrow {
            DummyApplication::class.java
        }
    }

}
