package org.eazyportal.plugin.gradle.portal.common.model

enum class ProjectTypes(
    val suffix: String,
) {

    API("-api"),
    APPLICATION("-application"),
    BEHEMOTH("-behemoth"),
    CLIENT("-client"),
    COMMON("-common"),
    DAO("-dao"),
    ROOT(""),
    SERVICE("-service"),
    WEB("-web");

}
