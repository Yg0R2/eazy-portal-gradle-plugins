package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.gradle.release.extension.getOrElse
import org.eazyportal.plugin.gradle.release.model.EazyReleasePluginExtension
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.gradle.api.provider.ProviderFactory
import java.io.File

object ScmConfigFactory {

    fun create(
        eazyReleaseExtension: EazyReleasePluginExtension,
        projectFile: ProjectFile<File>,
        providerFactory: ProviderFactory,
        scmActions: ScmActions<File>
    ): ScmConfig =
        eazyReleaseExtension.scmConfig
            .getOrElse {
                val releaseBranch = providerFactory
                    .systemProperty("releaseBranch")
                    .getOrElse { ScmConfig.GIT_FLOW.releaseBranch }

                val featureBranch = providerFactory
                    .systemProperty("featureBranch")
                    .getOrElse { scmActions.getCurrentBranch(projectFile) }

                ScmConfig(featureBranch, releaseBranch, ScmConstants.REMOTE)
            }

}
