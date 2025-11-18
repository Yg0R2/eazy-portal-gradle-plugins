package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.gradle.release.model.EazyReleasePluginExtension
import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.scm.model.ConventionalCommitType
import org.gradle.api.provider.ProviderFactory

object ReleaseActionContextFactory {

    fun create(
        eazyReleaseExtension: EazyReleasePluginExtension,
        providerFactory: ProviderFactory,
    ): ReleaseActionContext =
        ReleaseActionContext(
            conventionalCommitTypes = eazyReleaseExtension.conventionalCommitTypes
                .get()
                .ifEmpty { ConventionalCommitType.DEFAULT_TYPES },
            isForceRelease = providerFactory
                .systemProperty("forceRelease")
                .orNull
                .toBoolean(),
        )

}