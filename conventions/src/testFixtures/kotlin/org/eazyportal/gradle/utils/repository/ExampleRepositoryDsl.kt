package org.eazyportal.gradle.utils.repository

/**
 * Restricts nested DSL receivers so `artifact { }` configure blocks cannot accidentally call into each other.
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class ExampleRepositoryDsl
