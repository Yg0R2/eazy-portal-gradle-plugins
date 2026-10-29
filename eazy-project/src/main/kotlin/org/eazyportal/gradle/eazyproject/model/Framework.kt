package org.eazyportal.gradle.eazyproject.model

/**
 * Framework flavour of `eazyportal-core`, selecting the `eazyportal-{framework}-core-bom` and the framework-specific modules.
 * Each constant carries its **explicit kebab name** (same trick as [ProjectType]), so a future multi-word framework needs no name normalization.
 */
enum class Framework(
    private val framework: String,
) {

    SPRING_BOOT("spring-boot");

    override fun toString(): String =
        framework

    companion object {
        val allFrameworks = Framework.entries.map(Framework::toString)
    }

}
