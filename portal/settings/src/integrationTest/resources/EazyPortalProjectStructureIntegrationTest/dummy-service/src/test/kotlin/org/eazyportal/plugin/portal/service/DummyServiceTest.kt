package org.eazyportal.plugin.portal.service

import org.eazyportal.plugin.portal.api.ApiTestFixtures
import org.eazyportal.plugin.portal.dao.DummyDao
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DummyServiceTest {

    // @MockK
    private val dummyDao = DummyDao()

    // @InjectMockKs
    private val underTest = DummyService(dummyDao)

    @Test
    fun create() {
        val actual = underTest.create(ApiTestFixtures.DUMMY_REQUEST)

        val expected = ApiTestFixtures.DUMMY_RESPONSE.copy(
            id = actual.id
        )

        assertEquals(expected, actual)
    }

}
