package org.eazyportal.plugin.portal.behemoth

import org.eazyportal.plugin.portal.api.DummyClient
import org.eazyportal.plugin.portal.api.model.DummyRequest
import org.eazyportal.plugin.portal.api.model.DummyResponse
import org.eazyportal.plugin.portal.service.DummyService

class DummyBehemothClient(
    private val dummyService: DummyService,
) : DummyClient {

    override fun create(dummyRequest: DummyRequest): DummyResponse =
        dummyService.create(dummyRequest)

}
