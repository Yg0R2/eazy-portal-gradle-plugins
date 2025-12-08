package org.eazyportal.plugin.common.junit

import org.junit.jupiter.api.Named
import kotlin.reflect.KClass

@Deprecated("not needed anymore")
fun <T : Any> classNamed(payload: T): Named<T> =
    if (payload is KClass<*>) {
        payload
    } else {
        payload::class
    }.let { Named.named(it.java.simpleName, payload) }
