package org.eazyportal.plugin.portal.dao

import org.eazyportal.plugin.portal.dao.model.DummyEntity
import org.eazyportal.plugin.portal.testfixtures.CommonTestFixtures

object DaoTestFixtures {

    val DAO_VALUES = listOf(
        CommonTestFixtures::class,
        DaoTestFixtures::class,
    )

    val DUMMY_ENTITY = DummyEntity(
        id = CommonTestFixtures.DUMMY_ID,
        name = CommonTestFixtures.DUMMY_NAME
    )

}
