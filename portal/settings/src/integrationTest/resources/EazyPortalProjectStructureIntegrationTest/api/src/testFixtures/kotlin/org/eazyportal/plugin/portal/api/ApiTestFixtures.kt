package org.eazyportal.plugin.portal.api

import org.eazyportal.plugin.portal.api.model.DummyRequest
import org.eazyportal.plugin.portal.api.model.DummyResponse
import org.eazyportal.plugin.portal.testfixtures.CommonTestFixtures

object ApiTestFixtures {

    val API_VALUES = listOf(
        CommonTestFixtures::class,
        ApiTestFixtures::class,
    )

    val DUMMY_REQUEST = DummyRequest(
        name = CommonTestFixtures.DUMMY_NAME
    )

    val DUMMY_RESPONSE = DummyResponse(
        id = CommonTestFixtures.DUMMY_ID,
        name = CommonTestFixtures.DUMMY_NAME
    )

}
