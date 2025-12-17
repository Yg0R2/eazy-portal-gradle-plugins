package org.eazyportal.plugin.gradle.release.dsl

import org.eazyportal.plugin.gradle.release.dsl.model.ProjectDir
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

class SingleModuleScmProjectGiven(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
    val projectDir: ProjectDir,
    initScmProjectBlock: () -> Unit,
) : BaseScmProjectGiven(
    scmActions,
    scmConfig,
    initScmProjectBlock,
)
