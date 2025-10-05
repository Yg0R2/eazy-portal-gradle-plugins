package org.eazyportal.plugin.gradle.convetions

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

val Project.libs
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

val VersionCatalog.bundle
    get() = { key: String ->
        findBundle(key).get()
    }

val VersionCatalog.library
    get() = { key: String ->
        findLibrary(key).get()
    }

val VersionCatalog.plugin
    get() = { key: String ->
        findPlugin(key).get()
    }

val VersionCatalog.version
    get() = { key: String ->
        findVersion(key).get().requiredVersion
    }
