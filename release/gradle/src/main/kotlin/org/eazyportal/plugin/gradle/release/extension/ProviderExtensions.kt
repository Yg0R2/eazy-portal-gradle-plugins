package org.eazyportal.plugin.gradle.release.extension

import org.gradle.api.provider.Provider

fun <T : Any> Provider<T>.getOrElse(defaultValueBlock: () -> T) =
    orNull ?: defaultValueBlock()
