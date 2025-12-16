package org.eazyportal.plugin.gradle.release.testcase.dsl

class SingleModuleScmProjectGiven(
    private val context: SingleModuleScmProjectContext,
    private val initializeProjectBlock: (SingleModuleScmProjectContext, (SingleModuleScmProjectContext) -> Unit) -> Unit,
) : ScmProjectGiven<SingleModuleScmProjectContext>(
    context,
    initializeProjectBlock,
)
