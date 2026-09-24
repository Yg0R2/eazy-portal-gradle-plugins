package org.eazyportal.gradle.utils.gradle

/**
 * Restricts nested DSL receivers so configure blocks cannot accidentally call into each other.
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class GradleRunnerDsl()
