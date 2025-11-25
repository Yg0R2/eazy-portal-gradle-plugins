package org.eazyportal.plugin.gradle

import org.eazyportal.plugin.common.scm.ScmUtils

abstract class MultiModuleScmProjectBaseIntegrationTest(
    override val scmUtils: ScmUtils,
) : ScmProjectBaseIntegrationTest(scmUtils) {

}
