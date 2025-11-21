package org.eazyportal.plugin.portal.dao.model

import org.eazyportal.plugin.portal.testfixtures.CommonTestFixtures
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DummyEntityTest {

    @Test
    fun test() {
        val dummyEntity = DummyEntity(
            id = CommonTestFixtures.DUMMY_ID,
            name = CommonTestFixtures.DUMMY_NAME
        )

        Assertions.assertEquals(CommonTestFixtures.DUMMY_ID, dummyEntity.id)
    }

}