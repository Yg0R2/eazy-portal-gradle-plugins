package org.eazyportal.plugin.portal

import org.eazyportal.plugin.portal.api.ApiTestFixtures
import org.eazyportal.plugin.portal.dao.DaoTestFixtures
import org.eazyportal.plugin.portal.service.ServiceTestFixtures
import org.eazyportal.plugin.portal.testfixtures.CommonTestFixtures
import org.eazyportal.plugin.portal.web.WebTestFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestFixtureValuesTest {

    @Test
    fun test() {
        assertEquals(1, CommonTestFixtures.COMMON_VALUES.size)

        assertEquals(2, ApiTestFixtures.API_VALUES.size)
        assertEquals(2, DaoTestFixtures.DAO_VALUES.size)

        assertEquals(4, ServiceTestFixtures.SERVICE_VALUES.size)

        assertEquals(5, WebTestFixtures.WEB_VALUES.size)
    }

}
