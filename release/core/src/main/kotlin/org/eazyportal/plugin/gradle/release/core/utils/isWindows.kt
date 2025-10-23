package org.eazyportal.plugin.gradle.release.core.utils

fun isWindows(): Boolean =
    System.getProperty("os.name")
        .lowercase()
        .contains("windows")
