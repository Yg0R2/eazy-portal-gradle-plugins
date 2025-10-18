package org.eazyportal.plugin.portal.client

import org.eazyportal.plugin.portal.api.ApiTestFixtures
import org.eazyportal.plugin.portal.api.model.DummyRequest
import org.eazyportal.plugin.portal.api.model.DummyResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DummyFeignClientTest {

    private val underTest: DummyFeignClient = DummyFeignClientImpl()

    @Test
    fun test() {
        val actual = underTest.create(ApiTestFixtures.DUMMY_REQUEST)

        assertEquals(ApiTestFixtures.DUMMY_RESPONSE, actual)
    }

    private class DummyFeignClientImpl : DummyFeignClient {

        override fun create(dummyRequest: DummyRequest): DummyResponse =
            ApiTestFixtures.DUMMY_RESPONSE

    }

}