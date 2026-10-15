package org.eazyportal.gradle.eazyproject.model

/**
 * Name-based archetype for a receiver module (design §5.2). Each constant carries its **explicit kebab name**,
 * so a future multi-word archetype (e.g. `message-broker`) matches directly with no kebab↔enum-name normalization.
 * [isFrameworkSpecific] archetypes depend on `eazyportal-{framework}-core-{type}`; the others on the framework-agnostic `eazyportal-core-{type}`.
 */
enum class ProjectType(
    private val type: String,
    val isFrameworkSpecific: Boolean = false,
) {

    API("api"),
    APPLICATION("application", true),
    COMMON("common"),
    PERSISTENCE("persistence", true),
    SERVICE("service", true),
    CLIENT("client"),
    WEB("web", true),
    DEFAULT("default");

    override fun toString(): String =
        type

    companion object {
        /**
         * Case-insensitive match on the kebab-case module name; unknown names fall back to [DEFAULT] —
         * intentional and silent, by design (no override DSL, no WARN — see design §5.2).
         */
        fun fromProjectName(name: String): ProjectType =
            entries.firstOrNull {
                it.type.equals(name, true)
            } ?: DEFAULT
    }

}
