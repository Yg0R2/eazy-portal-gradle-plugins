package org.eazyportal.plugin.gradle.release.core.version

import org.eazyportal.plugin.gradle.release.core.version.exception.InvalidVersionException
import org.eazyportal.plugin.gradle.release.core.version.model.Version
import org.eazyportal.plugin.gradle.release.core.version.model.VersionIncrement

class ReleaseVersionProvider {

    /**
     * Returns with the release version.
     */
    fun provide(version: Version, versionIncrement: VersionIncrement): Version =
        when (versionIncrement) {
            VersionIncrement.MAJOR -> Version(version.major + 1, 0, 0)
            VersionIncrement.MINOR -> Version(version.major, version.minor + 1, 0)
            VersionIncrement.PATCH -> {
                if (!version.preRelease.isNullOrBlank() || !version.build.isNullOrBlank()) {
                    Version(version.major, version.minor, version.patch)
                } else {
                    Version(version.major, version.minor, version.patch + 1)
                }
            }

            VersionIncrement.NONE -> throw InvalidVersionException(
                "Cannot provide release version with '${VersionIncrement.NONE}' version increment for version: $version",
            )
        }

}
