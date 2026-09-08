package org.eazyportal.gradle.internal.assertion.extension

import org.assertj.core.api.AbstractStringAssert
import java.util.regex.Pattern

fun AbstractStringAssert<*>.taskDidRun(taskName: String): AbstractStringAssert<*> =
    containsPattern(createTaskNamePattern(taskName))

fun AbstractStringAssert<*>.taskDidNotRun(taskName: String): AbstractStringAssert<*> =
    doesNotContainPattern(createTaskNamePattern(taskName))

/**
 * A plain `contains("> Task :$taskName")` false-matches e.g. `:test` against `:testFixturesJar` —
 * anchor on the task-line boundary (task name followed by whitespace/EOL) so `test` only matches the `test` task itself.
 */
private fun createTaskNamePattern(taskName: String): Pattern =
    "(?m)^> Task :$taskName(\\s|$)".toPattern()
