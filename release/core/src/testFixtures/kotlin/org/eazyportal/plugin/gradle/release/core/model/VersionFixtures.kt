package org.eazyportal.plugin.gradle.release.core.model

import org.eazyportal.plugin.gradle.release.core.version.model.Version

object VersionFixtures {

    val RELEASE_001 = Version(0, 0, 1)
    val RELEASE_002 = Version(0, 0, 2)
    val RELEASE_003 = Version(0, 0, 3)

    val RELEASE_010 = Version(0, 1, 0)
    val RELEASE_020 = Version(0, 2, 0)

    val RELEASE_100 = Version(1, 0, 0)
    val RELEASE_200 = Version(2, 0, 0)

    val SNAPSHOT_001 = Version(0, 0, 1, Version.DEVELOPMENT_VERSION_SUFFIX)
    val SNAPSHOT_002 = Version(0, 0, 2, Version.DEVELOPMENT_VERSION_SUFFIX)

    val SNAPSHOT_010 = Version(0, 1, 0, Version.DEVELOPMENT_VERSION_SUFFIX)
    val SNAPSHOT_020 = Version(0, 2, 0, Version.DEVELOPMENT_VERSION_SUFFIX)

    val SNAPSHOT_100 = Version(1, 0, 0, Version.DEVELOPMENT_VERSION_SUFFIX)
    val SNAPSHOT_200 = Version(2, 0, 0, Version.DEVELOPMENT_VERSION_SUFFIX)

}
