package org.eazyportal.plugin.portal.client

import org.eazyportal.plugin.portal.api.ApiTestFixtures
import org.eazyportal.plugin.portal.testfixtures.CommonTestFixtures

object ClientTestFixtures {

    val CLIENT_VALUES = listOf(
        CommonTestFixtures::class,
        ApiTestFixtures::class,
        ClientTestFixtures::class,
    )

}
