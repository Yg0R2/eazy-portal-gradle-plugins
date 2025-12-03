package org.eazyportal.plugin.release.core

import org.eazyportal.plugin.common.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.release.core.executor.CommandExecutor
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.GitActions

class TestGitActions<T : Any>(
    commandExecutor: CommandExecutor<ProjectFile<T>>,
) : GitActions<T>(commandExecutor), TestScmActions<T> {

    override fun addSubmodule(
        projectFile: ProjectFile<T>,
        submoduleProjectFile: ProjectFile<T>,
    ) {
        execute(
            projectFile,
//            "-c",
//            "protocol.file.allow=always",
            "submodule",
            "add",
            "file:///${submoduleProjectFile.resolve(".git").getPath()}",
            submoduleProjectFile.getName(),
        )
    }

    override fun clean(projectFile: ProjectFile<T>) {
        super.clean(projectFile)

        execute(projectFile, "reset", "--hard")
    }

    override fun clone(
        from: ProjectFile<T>,
        to: ProjectFile<T>,
    ) {
        execute(
            to.getParent(),
            "-c",
            "protocol.file.allow=always",
            "clone",
            "--recurse-submodules",
            from.resolve(".git").getPath().toString(),
            to.getName(),
        )
    }

    override fun initializeRepository(
        projectFile: ProjectFile<T>,
        branchName: String,
    ) {
        projectFile.resolve("README.adoc")
            .writeText("= EazyPortal - $PROJECT_NAME")

        execute(projectFile, "init", "--initial-branch=$branchName")
        add(projectFile, ".")
        commit(projectFile, "initial commit")

        // TODO: move somewhere for origin specific
        // Workaround for using none-bare repository as origin
        execute(projectFile, "config", "receive.denyCurrentBranch", "ignore")
    }

    override fun status(projectFile: ProjectFile<T>): List<String> =
        execute(projectFile, "status")

}
