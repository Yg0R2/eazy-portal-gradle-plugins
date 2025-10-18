package org.eazyportal.plugin.portal.behemoth

import org.eazyportal.plugin.portal.api.ApiTestFixtures
import org.eazyportal.plugin.portal.service.ServiceTestFixtures
import org.eazyportal.plugin.portal.testfixtures.CommonTestFixtures

object BehemothTestFixtures {

    val BEHEMOTH_VALUES = listOf(
        CommonTestFixtures::class,
        ApiTestFixtures::class,
        ServiceTestFixtures::class,
        BehemothTestFixtures::class,
    )

}
