package org.eazyportal.plugin.gradle.release.core.version

import org.eazyportal.plugin.gradle.release.core.version.exception.InvalidVersionException
import org.eazyportal.plugin.gradle.release.core.version.model.Version

class SnapshotVersionProvider {

    /**
     * Returns with the next SNAPSHOT version.
     */
    fun provide(version: Version): Version =
        if (version.isRelease()) {
            Version(version.major, version.minor, version.patch + 1, Version.DEVELOPMENT_VERSION_SUFFIX)
        } else {
            throw InvalidVersionException(
                "Project already on ${Version.DEVELOPMENT_VERSION_SUFFIX} version."
            )
        }

}
