package org.eazyportal.plugin.gradle.portal.common.model

import org.gradle.api.Project

enum class ProjectTypes(
    val suffix: String,
) {

    API("-api"),
    APPLICATION("-application"),
    BEHEMOTH("-behemoth"),
    CLIENT("-client"),
    COMMON("-common"),
    DAO("-dao"),
    SERVICE("-service"),
    WEB("-web");

    companion object {
        fun Project.isTypeOf(projectType: ProjectTypes): Boolean =
            project.name.endsWith(projectType.suffix)
    }

}
