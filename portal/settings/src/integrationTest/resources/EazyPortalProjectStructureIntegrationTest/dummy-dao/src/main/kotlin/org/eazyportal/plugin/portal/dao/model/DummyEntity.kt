package org.eazyportal.plugin.portal.dao.model

import java.util.UUID

data class DummyEntity(
    val id: UUID = UUID.randomUUID(),
    val name: String
)