package org.eazyportal.plugin.gradle.portal.project.configurer

import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes
import org.eazyportal.plugin.gradle.portal.project.model.ProjectServiceParameters
import org.gradle.api.Project

object ProjectConfigurerFactory {

    fun createProjectConfigurer(
        projectParameters: ProjectServiceParameters,
        project: Project,
    ): ProjectConfigurer =
        when (projectParameters.projectTypeMap.get()[project.path]) {
            ProjectTypes.API -> ApiProjectConfigurer(projectParameters, project)
            ProjectTypes.APPLICATION -> ApplicationProjectConfigurer(projectParameters, project)
            ProjectTypes.BEHEMOTH -> BehemothProjectConfigurer(projectParameters, project)
            ProjectTypes.CLIENT -> ClientProjectConfigurer(projectParameters, project)
            ProjectTypes.COMMON -> CommonProjectConfigurer(projectParameters, project)
            ProjectTypes.DAO -> DaoProjectConfigurer(projectParameters, project)
            ProjectTypes.ROOT -> RootProjectConfigurer(projectParameters, project)
            ProjectTypes.SERVICE -> ServiceProjectConfigurer(projectParameters, project)
            ProjectTypes.WEB -> WebProjectConfigurer(projectParameters, project)
            else -> GradleProjectConfigurer(projectParameters, project)
        }

}
