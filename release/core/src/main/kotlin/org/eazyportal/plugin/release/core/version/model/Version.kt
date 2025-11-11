package org.eazyportal.plugin.release.core.version.model

import org.eazyportal.plugin.release.core.version.VersionComparator

data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int,
    var preRelease: String? = null,
    var build: String? = null
) {

    init {
        if ((major < 0) || (minor < 0) || (patch < 0)) {
            throw IllegalArgumentException("Version cannot have negative major, minor, or patch values.")
        }

        if (preRelease.isNullOrBlank()) {
            preRelease = null
        } else if (preRelease!!.startsWith("0")) {
            // https://semver.org/#spec-item-9
            throw IllegalArgumentException("Pre-release should not start with '0'.")
        }

        if (build.isNullOrBlank()) {
            build = null
        }
    }

    fun isRelease(): Boolean =
        preRelease.isNullOrBlank() && build.isNullOrBlank()

    override fun toString(): String {
        val sb = StringBuilder(9)

        sb.append(major)
        sb.append(".")
        sb.append(minor)
        sb.append(".")
        sb.append(patch)

        preRelease?.run {
            sb.append("-")
            sb.append(this)
        }

        build?.run {
            sb.append("+")
            sb.append(this)
        }

        return sb.toString()
    }

    operator fun compareTo(other: Version?): Int =
        VersionComparator().compare(this, other)

    companion object {
        const val DEVELOPMENT_VERSION_SUFFIX = "SNAPSHOT"

        private val VERSION_REGEX = "^(\\d+)\\.(\\d+)\\.(\\d+)-?([a-zA-Z-\\d\\.]*)\\+?([a-zA-Z-\\d\\.]*)$".toRegex()

        fun of(versionValue: String): Version =
            VERSION_REGEX.find(versionValue)
                ?.destructured
                ?.let { (major, minor, patch, preRelease, build) ->
                    Version(major.toInt(), minor.toInt(), patch.toInt(), preRelease, build)
                } ?: throw IllegalArgumentException("Failed to parse provided version: $versionValue")
    }

}
