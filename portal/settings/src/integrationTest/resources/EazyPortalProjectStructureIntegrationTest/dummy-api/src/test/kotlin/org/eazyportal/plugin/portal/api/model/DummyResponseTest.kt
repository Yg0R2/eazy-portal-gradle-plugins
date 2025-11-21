package org.eazyportal.plugin.portal.api.model

import org.eazyportal.plugin.portal.api.ApiTestFixtures
import org.eazyportal.plugin.portal.testfixtures.CommonTestFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DummyResponseTest {

    @Test
    fun test() {
        val dummyResponse = DummyResponse(
            id = CommonTestFixtures.DUMMY_ID,
            name = CommonTestFixtures.DUMMY_NAME,
        )

        assertEquals(ApiTestFixtures.DUMMY_RESPONSE, dummyResponse)
    }

}