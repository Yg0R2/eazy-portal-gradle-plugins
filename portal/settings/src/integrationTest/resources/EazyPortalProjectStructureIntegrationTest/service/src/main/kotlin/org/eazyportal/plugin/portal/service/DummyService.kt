package org.eazyportal.plugin.portal.service

import org.eazyportal.plugin.portal.api.model.DummyRequest
import org.eazyportal.plugin.portal.api.model.DummyResponse
import org.eazyportal.plugin.portal.dao.DummyDao
import org.eazyportal.plugin.portal.dao.model.DummyEntity

class DummyService(
    private val dummyDao: DummyDao,
) {

    fun create(dummyRequest: DummyRequest): DummyResponse =
        DummyEntity(
            name = dummyRequest.name,
        ).let {
            dummyDao.create(it)
        }.let {
            DummyResponse(
                id = it.id,
                name = it.name,
            )
        }

}
