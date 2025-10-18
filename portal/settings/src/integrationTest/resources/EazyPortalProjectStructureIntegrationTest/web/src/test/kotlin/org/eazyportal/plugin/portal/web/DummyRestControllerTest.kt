package org.eazyportal.plugin.portal.web

import org.eazyportal.plugin.portal.api.ApiTestFixtures
import org.eazyportal.plugin.portal.dao.DummyDao
import org.eazyportal.plugin.portal.service.DummyService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DummyRestControllerTest {

    // @MockK
    private val dummyDao = DummyDao()

    // @MockK
    private val dummyService = DummyService(dummyDao)

    // @InjectMockKs
    private val underTest = DummyRestController(dummyService)

    @Test
    fun create() {
        val actual = underTest.create(ApiTestFixtures.DUMMY_REQUEST)

        val expected = ApiTestFixtures.DUMMY_RESPONSE.copy(
            id = actual.id
        )

        assertEquals(expected, actual)
    }

}
