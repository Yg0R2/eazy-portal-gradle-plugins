package org.eazyportal.gradle.eazyproject.configurer

/**
 * Configures a single receiver module for its archetype (design §5.4):
 * - applies the matching convention,
 * - imports the `eazyportal-{framework}-core-bom`,
 * - wires sibling projects,
 * - and adds eazyportal-core dependencies at the correct configuration level.
 *
 * Obtain the right implementation from [ProjectConfigurerFactory.forType];
 * [GradleProjectConfigurer] is the shared template every archetype extends.
 */
interface ProjectConfigurer {

    fun configure()

}
