package org.eazyportal.plugin.portal.api

import org.eazyportal.plugin.portal.api.model.DummyRequest
import org.eazyportal.plugin.portal.api.model.DummyResponse

interface DummyClient {

    fun create(dummyRequest: DummyRequest): DummyResponse

}
