package org.eazyportal.plugin.portal.web

import org.eazyportal.plugin.portal.api.model.DummyRequest
import org.eazyportal.plugin.portal.api.model.DummyResponse
import org.eazyportal.plugin.portal.service.DummyService

class DummyRestController(
    private val dummyService: DummyService,
) {

    fun create(dummyRequest: DummyRequest): DummyResponse =
        dummyService.create(dummyRequest)

}
