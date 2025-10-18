package org.eazyportal.plugin.portal.service

import org.eazyportal.plugin.portal.api.ApiTestFixtures
import org.eazyportal.plugin.portal.dao.DaoTestFixtures
import org.eazyportal.plugin.portal.testfixtures.CommonTestFixtures

object ServiceTestFixtures {

    val SERVICE_VALUES = listOf(
        CommonTestFixtures::class,
        ApiTestFixtures::class,
        DaoTestFixtures::class,
        ServiceTestFixtures::class,
    )

}
