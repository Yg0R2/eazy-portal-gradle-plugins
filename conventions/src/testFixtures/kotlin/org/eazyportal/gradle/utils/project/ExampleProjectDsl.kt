package org.eazyportal.gradle.utils.project

/**
 * Restricts nested DSL receivers so `settings { }`, `rootProject { }`, and `subproject { }`
 * configure blocks cannot accidentally call into each other.
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class ExampleProjectDsl
