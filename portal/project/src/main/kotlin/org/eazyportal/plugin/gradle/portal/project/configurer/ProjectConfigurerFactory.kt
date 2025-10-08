package org.eazyportal.plugin.gradle.portal.project.configurer

import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes
import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes.Companion.isTypeOf
import org.gradle.api.Project

object ProjectConfigurerFactory {

    fun createProjectConfigurer(
        project: Project,
    ): ProjectConfigurer {
        val type = ProjectTypes.entries.firstOrNull { project.isTypeOf(it) }

        return when (type) {
            ProjectTypes.API -> ApiProjectConfigurer(project)
            ProjectTypes.APPLICATION -> ApplicationProjectConfigurer(project)
            ProjectTypes.BEHEMOTH -> BehemothProjectConfigurer(project)
            ProjectTypes.CLIENT -> ClientProjectConfigurer(project)
            ProjectTypes.COMMON -> CommonProjectConfigurer(project)
            ProjectTypes.DAO -> DaoProjectConfigurer(project)
            ProjectTypes.SERVICE -> ServiceProjectConfigurer(project)
            ProjectTypes.WEB -> WebProjectConfigurer(project)
            else -> GradleProjectConfigurer(project)
        }
    }

}
