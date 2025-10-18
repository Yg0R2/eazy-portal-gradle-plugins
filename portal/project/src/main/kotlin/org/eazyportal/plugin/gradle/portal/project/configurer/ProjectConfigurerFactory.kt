package org.eazyportal.plugin.gradle.portal.project.configurer

import org.eazyportal.plugin.gradle.portal.common.model.EazyPortalServiceParameters
import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes
import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes.Companion.isTypeOf
import org.gradle.api.Project

object ProjectConfigurerFactory {

    fun createProjectConfigurer(
        parameters: EazyPortalServiceParameters,
        project: Project,
    ): ProjectConfigurer {
        val type = ProjectTypes.entries.firstOrNull { project.isTypeOf(it) }

        return when (type) {
            ProjectTypes.API -> ApiProjectConfigurer(parameters, project)
            ProjectTypes.APPLICATION -> ApplicationProjectConfigurer(parameters, project)
            ProjectTypes.BEHEMOTH -> BehemothProjectConfigurer(parameters, project)
            ProjectTypes.CLIENT -> ClientProjectConfigurer(parameters, project)
            ProjectTypes.COMMON -> CommonProjectConfigurer(parameters, project)
            ProjectTypes.DAO -> DaoProjectConfigurer(parameters, project)
            ProjectTypes.ROOT -> RootProjectConfigurer(parameters, project)
            ProjectTypes.SERVICE -> ServiceProjectConfigurer(parameters, project)
            ProjectTypes.WEB -> WebProjectConfigurer(parameters, project)
            else -> GradleProjectConfigurer(parameters, project)
        }
    }

}
