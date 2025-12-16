package org.eazyportal.plugin.gradle.release.testcase.dsl

class MultiModuleScmProjectGiven(
    private val context: MultiModuleScmProjectContext,
    private val initializeProjectBlock: (MultiModuleScmProjectContext, () -> Unit) -> Unit,
) : ScmProjectGiven<MultiModuleScmProjectContext>(
    context,
    initializeProjectBlock,
)
