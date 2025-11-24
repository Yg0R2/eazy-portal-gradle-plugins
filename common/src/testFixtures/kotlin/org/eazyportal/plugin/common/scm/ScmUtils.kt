package org.eazyportal.plugin.common.scm

import org.eazyportal.plugin.common.ScmTestFixtures.DUMMY_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.DUMMY_FILE_NAME
import org.eazyportal.plugin.common.cli.CommandLineUtils.git
import java.io.File
import java.util.UUID

object GitUtils : ScmUtils() {

    override fun createDummyCommit(
        workingDir: File,
        branch: String,
        commitMessage: String,
    ) {
        workingDir.git("checkout", branch)

        createDummyFile(workingDir)

        workingDir.git("add", DUMMY_FILE_NAME)
        workingDir.git("commit", "-m", commitMessage)
    }

    override fun createDummyFile(workingDir: File) {
        workingDir.resolve(DUMMY_FILE_NAME)
            .writeText(UUID.randomUUID().toString())
    }

}

abstract class ScmUtils {

    abstract fun createDummyCommit(
        workingDir: File,
        branch: String,
        commitMessage: String = DUMMY_COMMIT_MESSAGE,
    )

    abstract fun createDummyFile(workingDir: File)

}
