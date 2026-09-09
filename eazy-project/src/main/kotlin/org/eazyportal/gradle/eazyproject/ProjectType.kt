package org.eazyportal.gradle.eazyproject

/**
 * Name-based archetype for a receiver module (design §5.2). Each constant carries its **explicit kebab name**,
 * so a future multi-word archetype (e.g. `message-broker`) matches directly with no kebab↔enum-name normalization.
 */
enum class ProjectType(
    private val type: String,
) {

    API("api"),
    APPLICATION("application"),
    COMMON("common"),
    PERSISTENCE("persistence"),
    SERVICE("service"),
    CLIENT("client"),
    WEB("web"),
    DEFAULT("default");

    companion object {
        /**
         * Case-insensitive match on the kebab-case module name; unknown names fall back to [DEFAULT] —
         * intentional and silent, by design (no override DSL, no WARN — see design §5.2).
         */
        fun fromProjectName(name: String): ProjectType =
            entries.firstOrNull {
                it.type.equals(name, ignoreCase = true)
            } ?: DEFAULT
    }
}
