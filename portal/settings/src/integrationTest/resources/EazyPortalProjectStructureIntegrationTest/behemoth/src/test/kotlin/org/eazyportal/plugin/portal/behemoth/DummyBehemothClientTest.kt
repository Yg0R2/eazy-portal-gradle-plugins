package org.eazyportal.plugin.portal.behemoth

import org.eazyportal.plugin.portal.api.ApiTestFixtures
import org.eazyportal.plugin.portal.dao.DummyDao
import org.eazyportal.plugin.portal.service.DummyService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DummyBehemothClientTest {

    private val dummyDao = DummyDao()

    private val dummyService = DummyService(dummyDao)

    private val underTest = DummyBehemothClient(dummyService)

    @Test
    fun test() {
        val actual = underTest.create(ApiTestFixtures.DUMMY_REQUEST)

        val expected = ApiTestFixtures.DUMMY_RESPONSE.copy(
            id = actual.id
        )

        assertEquals(expected, actual)
    }

}
