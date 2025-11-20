package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.gradle.release.extension.getOrElse
import org.eazyportal.plugin.gradle.release.model.EazyReleasePluginExtension
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.gradle.api.provider.ProviderFactory

object ScmConfigFactory {

    fun create(
        eazyReleaseExtension: EazyReleasePluginExtension,
        providerFactory: ProviderFactory,
    ): ScmConfig =
        eazyReleaseExtension.scmConfig
            .getOrElse {
                val releaseBranch = providerFactory
                    .systemProperty("releaseBranch")
                    .getOrElse { ScmConfig.GIT_FLOW.releaseBranch }

                val featureBranch = providerFactory
                    .systemProperty("featureBranch")
                    .getOrElse { ScmConfig.GIT_FLOW.featureBranch }

                ScmConfig(featureBranch, releaseBranch, ScmConstants.REMOTE)
            }

}
