package org.eazyportal.gradle.eazyproject.configurer

import org.eazyportal.gradle.eazyproject.model.EazyProjectExtension
import org.eazyportal.gradle.eazyproject.model.ProjectType
import org.gradle.api.Project

/** Maps a [ProjectType] to its [ProjectConfigurer]. */
internal object ProjectConfigurerFactory {

    fun forType(
        eazyProjectExtension: EazyProjectExtension,
        project: Project,
    ): ProjectConfigurer =
        when (ProjectType.fromProjectName(project.name)) {
            ProjectType.COMMON -> CommonProjectConfigurer(eazyProjectExtension, project)
            ProjectType.API -> ApiProjectConfigurer(eazyProjectExtension, project)
            ProjectType.PERSISTENCE -> PersistenceProjectConfigurer(eazyProjectExtension, project)
            ProjectType.SERVICE -> ServiceProjectConfigurer(eazyProjectExtension, project)
            ProjectType.CLIENT -> ClientProjectConfigurer(eazyProjectExtension, project)
            ProjectType.WEB -> WebProjectConfigurer(eazyProjectExtension, project)
            ProjectType.APPLICATION -> ApplicationProjectConfigurer(eazyProjectExtension, project)
            ProjectType.DEFAULT -> DefaultProjectConfigurer(eazyProjectExtension, project)
        }

}
