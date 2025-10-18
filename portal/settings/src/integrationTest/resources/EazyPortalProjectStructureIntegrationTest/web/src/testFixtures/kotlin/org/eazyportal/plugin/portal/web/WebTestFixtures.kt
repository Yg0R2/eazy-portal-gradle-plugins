package org.eazyportal.plugin.portal.web

import org.eazyportal.plugin.portal.api.ApiTestFixtures
import org.eazyportal.plugin.portal.dao.DaoTestFixtures
import org.eazyportal.plugin.portal.service.ServiceTestFixtures
import org.eazyportal.plugin.portal.testfixtures.CommonTestFixtures

object WebTestFixtures {

    val WEB_VALUES = listOf(
        CommonTestFixtures::class,
        ApiTestFixtures::class,
        DaoTestFixtures::class,
        ServiceTestFixtures::class,
        WebTestFixtures::class,
    )

}
