package org.eazyportal.plugin.release.core.utils

fun isWindows(): Boolean =
    System.getProperty("os.name")
        .lowercase()
        .contains("windows")
