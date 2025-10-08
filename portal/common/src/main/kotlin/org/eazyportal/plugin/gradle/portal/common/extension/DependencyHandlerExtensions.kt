package org.eazyportal.plugin.gradle.portal.common.extension

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler

fun DependencyHandler.addAs(
    configurationName: String,
    dependencyNotation: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? =
    dependencyNotation?.let {
        val moduleDependency = add(configurationName, dependencyNotation) as ModuleDependency

        moduleDependency.apply {
            block(this)
        }
    }

fun DependencyHandler.addAs(
    configurationName: String,
    group: String,
    name: String,
    version: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? {
    val versionNotation = version.takeIf { it != null }
        ?.let { ":$it" }
        ?: ""

    return addAs(configurationName, "$group:$name$versionNotation", block)
}

fun DependencyHandler.addAsApi(
    dependencyNotation: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? =
    addAs("api", dependencyNotation, block)

fun DependencyHandler.addAsApi(
    group: String,
    name: String,
    version: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? =
    addAs("api", group, name, version, block)

fun DependencyHandler.addAsCompileOnly(
    dependencyNotation: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? =
    addAs("compileOnly", dependencyNotation, block)

fun DependencyHandler.addAsCompileOnly(
    group: String,
    name: String,
    version: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? =
    addAs("compileOnly", group, name, version, block)

fun DependencyHandler.addAsImplementation(
    dependencyNotation: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? =
    addAs("implementation", dependencyNotation, block)

fun DependencyHandler.addAsImplementation(
    group: String,
    name: String,
    version: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? =
    addAs("implementation", group, name, version, block)

fun DependencyHandler.addAsIntegrationTestApi(
    dependencyNotation: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? =
    addAs("integrationTestApi", dependencyNotation, block)

fun DependencyHandler.addAsIntegrationTestApi(
    group: String,
    name: String,
    version: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? =
    addAs("integrationTestApi", group, name, version, block)

fun DependencyHandler.addAsIntegrationTestImplementation(
    dependencyNotation: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? =
    addAs("integrationTestImplementation", dependencyNotation, block)

fun DependencyHandler.addAsIntegrationTestImplementation(
    group: String,
    name: String,
    version: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? =
    addAs("integrationTestImplementation", group, name, version, block)

fun DependencyHandler.addAsTestFixturesApi(
    dependencyNotation: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? =
    addAs("testFixturesApi", dependencyNotation, block)

fun DependencyHandler.addAsTestFixturesApi(
    group: String,
    name: String,
    version: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? =
    addAs("testFixturesApi", group, name, version, block)

fun DependencyHandler.addAsTestFixturesImplementation(
    dependencyNotation: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? =
    addAs("testFixturesImplementation", dependencyNotation, block)

fun DependencyHandler.addAsTestFixturesImplementation(
    group: String,
    name: String,
    version: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? =
    addAs("testFixturesImplementation", group, name, version, block)

fun DependencyHandler.addAsTestApi(
    dependencyNotation: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? =
    addAs("testApi", dependencyNotation, block)

fun DependencyHandler.addAsTestApi(
    group: String,
    name: String,
    version: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? =
    addAs("testApi", group, name, version, block)

fun DependencyHandler.addAsTestImplementation(
    dependencyNotation: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? =
    addAs("testImplementation", dependencyNotation, block)

fun DependencyHandler.addAsTestImplementation(
    group: String,
    name: String,
    version: Any?,
    block: ModuleDependency.() -> Unit = {}
): ModuleDependency? =
    addAs("testImplementation", group, name, version, block)

fun DependencyHandler.testFixtures(project: Project?): Dependency? =
    project?.let { testFixtures(project) }

fun DependencyHandler.testFixtures(
    group: String,
    name: String,
    version: Any?,
): Dependency {
    val versionNotation = version.takeIf { it != null }
        ?.let { ":$it" }
        ?: ""

    return testFixtures("$group:$name$versionNotation")
}
