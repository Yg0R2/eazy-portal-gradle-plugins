package org.eazyportal.plugin.release.core.version.model

enum class VersionIncrement(
    val priority: Int
) {

    ERROR(-1),
    MAJOR(0),
    MINOR(1),
    PATCH(2),
    NONE(3),

}
