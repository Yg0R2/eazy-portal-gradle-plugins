package org.eazyportal.plugin.portal.dao

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DummyDaoTest {

    private val underTest = DummyDao()

    @Test
    fun create() {
        assertEquals(DaoTestFixtures.DUMMY_ENTITY, underTest.create(DaoTestFixtures.DUMMY_ENTITY))
    }

}
