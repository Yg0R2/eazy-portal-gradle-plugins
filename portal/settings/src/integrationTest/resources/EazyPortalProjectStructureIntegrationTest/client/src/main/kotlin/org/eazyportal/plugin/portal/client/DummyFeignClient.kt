package org.eazyportal.plugin.portal.client

import org.eazyportal.plugin.portal.api.DummyClient
import org.eazyportal.plugin.portal.api.model.DummyRequest
import org.eazyportal.plugin.portal.api.model.DummyResponse

interface DummyFeignClient : DummyClient {

    override fun create(dummyRequest: DummyRequest): DummyResponse

}
