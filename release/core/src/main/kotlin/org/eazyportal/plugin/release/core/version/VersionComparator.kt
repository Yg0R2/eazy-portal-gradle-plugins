package org.eazyportal.plugin.release.core.version

import org.eazyportal.plugin.release.core.version.model.Version
import kotlin.math.min

internal class VersionComparator : Comparator<Version?> {

    override fun compare(left: Version?, right: Version?): Int {
        if ((left == null) && (right == null)) {
            return 0
        } else if (left == null) {
            return 1
        } else if (right == null) {
            return -1
        }

        left.major.compareTo(right.major)
            .takeIf { it != 0 }
            ?.run { return this }

        left.minor.compareTo(right.minor)
            .takeIf { it != 0 }
            ?.run { return this }

        left.patch.compareTo(right.patch)
            .takeIf { it != 0 }
            ?.run { return this }

        // https://semver.org/#spec-item-11
        comparePreRelease(left.preRelease, right.preRelease)
            .takeIf { it != 0 }
            ?.run { return this }

        // build metadata is ignored from the comparison: https://semver.org/#spec-item-10
        return 0
    }

    private fun castPreReleasePart(part: String): Any =
        runCatching {
            part.toInt()
        }.getOrElse { part }

    private fun comparePreRelease(leftPreRelease: String?, rightPreRelease: String?): Int {
        if ((leftPreRelease == null) && (rightPreRelease == null)) {
            return 0
        } else if (leftPreRelease == null) {
            return -1
        } else if (rightPreRelease == null) {
            return 1
        }

        val leftPreReleaseParts = leftPreRelease.split(".")
        val rightPreReleaseParts = rightPreRelease.split(".")

        val maxIndex = min(leftPreReleaseParts.size, rightPreReleaseParts.size)
        for (i: Int in 0 until maxIndex) {
            comparePreReleaseParts(leftPreReleaseParts[i], rightPreReleaseParts[i])
                .takeIf { it != 0 }
                ?.run { return this }
        }

        if (leftPreReleaseParts.size < rightPreReleaseParts.size) {
            return -1
        } else if (leftPreReleaseParts.size > rightPreReleaseParts.size) {
            return 1
        }

        return 0
    }

    private fun comparePreReleaseParts(leftPreReleasePart: String, rightPreReleasePart: String): Int {
        val leftCasted = castPreReleasePart(leftPreReleasePart)
        val rightCasted = castPreReleasePart(rightPreReleasePart)

        if (leftCasted is Int) {
            if (rightCasted is String) {
                return -1
            }

            leftCasted.compareTo(rightCasted as Int)
                .takeIf { it != 0 }
                ?.run { return this }
        } else if (leftCasted is String) {
            if (rightCasted is Int) {
                return 1
            }

            leftCasted.compareTo(rightCasted as String)
                .takeIf { it != 0 }
                ?.run { return this }
        }

        return 0
    }

}
