package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.common.integration.test.ProjectTestFixtures.GRADLE_PROPERTIES_FILE_NAME
import org.eazyportal.plugin.common.integration.test.ProjectTestFixtures.SUBMODULE_NAMES
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleCustomizedScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleGitFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleCustomizedScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleGitFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_002
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SetSnapshotVersionTaskIntegrationTest {

    interface SetSnapshotVersionTaskTestCase {

        fun `test 'run' should fail when project is already on SNASPSHOT version`()

        fun `test 'run' from release branch should set SNASPSHOT version`()

        fun `test 'run' from feature branch should set SNASPSHOT version`()

    }

    interface SetSnapshotVersionTaskSingleModuleTestCase :
        SetSnapshotVersionTaskTestCase,
        SingleModuleScmProjectTestCase {

        @Test
        override fun `test 'run' should fail when project is already on SNASPSHOT version`() = runTestCase {
            givenTestCase {
                withScmProject()
            }

            whenExecute {
                gradleTaskFails(SET_SNAPSHOT_VERSION_TASK_NAME)
            }

            thenVerify {
                gradleTaskOutput {
                    contains(
                        "> Task :$SET_SNAPSHOT_VERSION_TASK_NAME FAILED",
                        "Execution failed for task ':$SET_SNAPSHOT_VERSION_TASK_NAME'.",
                        "> Project already on SNAPSHOT version.",
                    )
                }
            }
        }

        @Test
        override fun `test 'run' from release branch should set SNASPSHOT version`() = runTestCase {
            givenTestCase {
                withScmProject()

                scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)

                setProjectVersion(projectDir.localProjectFile, RELEASE_001)
                scmActions.add(projectDir.localProjectFile, ".")
                scmActions.commit(projectDir.localProjectFile, "Release version: $RELEASE_001")
                scmActions.push(projectDir.localProjectFile, scmConfig.remote, scmConfig.releaseBranch)
            }

            whenExecute {
                gradleTaskSucceeds(SET_SNAPSHOT_VERSION_TASK_NAME)
            }

            thenVerify {
                gradleTaskOutput {
                    contains("> Task :$SET_SNAPSHOT_VERSION_TASK_NAME")
                }

                with(projectDir.localProjectFile) {
                    scmStatusIn(this) {
                        contains(
                            "On branch ${scmConfig.featureBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.featureBranch}'.",
                            "Changes to be committed:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                            "Changes not staged for commit:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                        )
                    }

                    projectVersionIn(this) {
                        isEqualTo(SNAPSHOT_002)
                    }
                }
            }
        }

        @Test
        override fun `test 'run' from feature branch should set SNASPSHOT version`() = runTestCase {
            givenTestCase {
                withScmProject()

                scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)

                setProjectVersion(projectDir.localProjectFile, RELEASE_001)
                scmActions.add(projectDir.localProjectFile, ".")
                scmActions.commit(projectDir.localProjectFile, "Release version: $RELEASE_001")
                scmActions.push(projectDir.localProjectFile, scmConfig.remote, scmConfig.featureBranch)
            }

            whenExecute {
                gradleTaskSucceeds(SET_SNAPSHOT_VERSION_TASK_NAME)
            }

            thenVerify {
                gradleTaskOutput {
                    contains("> Task :$SET_SNAPSHOT_VERSION_TASK_NAME")
                }

                with(projectDir.localProjectFile) {
                    scmStatusIn(this) {
                        contains(
                            "On branch ${scmConfig.featureBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.featureBranch}'.",
                            "Changes not staged for commit:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                            "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                        )
                    }

                    projectVersionIn(this) {
                        isEqualTo(SNAPSHOT_002)
                    }
                }
            }
        }

    }

    interface SetSnapshotVersionTaskMultiModuleTestCase :
        SetSnapshotVersionTaskTestCase,
        MultiModuleScmProjectTestCase {

        @Test
        override fun `test 'run' should fail when project is already on SNASPSHOT version`() = runTestCase {
            givenTestCase {
                withScmProject()
            }

            whenExecute {
                gradleTaskFails(SET_SNAPSHOT_VERSION_TASK_NAME)
            }

            thenVerify {
                gradleTaskOutput {
                    contains(
                        "> Task :$SET_SNAPSHOT_VERSION_TASK_NAME FAILED",
                        "Execution failed for task ':$SET_SNAPSHOT_VERSION_TASK_NAME'.",
                        "> Project already on SNAPSHOT version.",
                    )
                }
            }
        }

        @Test
        override fun `test 'run' from release branch should set SNASPSHOT version`() = runTestCase {
            givenTestCase {
                withScmProject()

                scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)

                allProjectDirs.forEach {
                    setProjectVersion(it.localProjectFile, RELEASE_001)
                    scmActions.add(it.localProjectFile, ".")
                    scmActions.commit(it.localProjectFile, "Release version: $RELEASE_001")
                    scmActions.push(it.localProjectFile, scmConfig.remote, scmConfig.releaseBranch)
                }
            }

            whenExecute {
                gradleTaskSucceeds(SET_SNAPSHOT_VERSION_TASK_NAME)
            }

            thenVerify {
                gradleTaskOutput {
                    contains("> Task :$SET_SNAPSHOT_VERSION_TASK_NAME")
                }

                with(projectDir.localProjectFile) {
                    scmStatusIn(this) {
                        contains(
                            "On branch ${scmConfig.featureBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.featureBranch}'.",
                            "Changes to be committed:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                            "Changes not staged for commit:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                        )
                    }

                    projectVersionIn(this) {
                        isEqualTo(SNAPSHOT_002)
                    }
                }

                submoduleProjectDirs.forEach {
                    scmStatusIn(it.localProjectFile) {
                        contains(
                            "On branch ${scmConfig.featureBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.featureBranch}'.",
                            "Changes to be committed:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                            "Changes not staged for commit:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                        )
                    }

                    projectVersionIn(it.localProjectFile) {
                        isEqualTo(SNAPSHOT_002)
                    }
                }
            }
        }

        @Test
        override fun `test 'run' from feature branch should set SNASPSHOT version`() = runTestCase {
            givenTestCase {
                withScmProject()

                scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)

                allProjectDirs.forEach {
                    setProjectVersion(it.localProjectFile, RELEASE_001)
                    scmActions.add(it.localProjectFile, ".")
                    scmActions.commit(it.localProjectFile, "Release version: $RELEASE_001")
                    scmActions.push(it.localProjectFile, scmConfig.remote, scmConfig.featureBranch)
                }
            }

            whenExecute {
                gradleTaskSucceeds(SET_SNAPSHOT_VERSION_TASK_NAME)
            }

            thenVerify {
                gradleTaskOutput {
                    contains("> Task :$SET_SNAPSHOT_VERSION_TASK_NAME")
                }

                with(projectDir.localProjectFile) {
                    scmStatusIn(this) {
                        contains(
                            "On branch ${scmConfig.featureBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.featureBranch}'.",
                            "Changes not staged for commit:",
                            *SUBMODULE_NAMES.map { "modified:   $it (modified content)" }
                                .toTypedArray(),
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                            "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                        )
                    }

                    projectVersionIn(this) {
                        isEqualTo(SNAPSHOT_002)
                    }
                }

                submoduleProjectDirs.forEach {
                    scmStatusIn(it.localProjectFile) {
                        contains(
                            "On branch ${scmConfig.featureBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.featureBranch}'.",
                            "Changes not staged for commit:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                            "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                        )
                    }

                    projectVersionIn(it.localProjectFile) {
                        isEqualTo(SNAPSHOT_002)
                    }
                }
            }
        }

    }

    @Nested
    inner class SingleModuleGitFlowTestCase :
        SetSnapshotVersionTaskSingleModuleTestCase,
        SingleModuleGitFlowScmProjectTestCase()

    @Nested
    inner class SingleModuleTrunkFlowTestCase :
        SetSnapshotVersionTaskSingleModuleTestCase,
        SingleModuleTrunkFlowScmProjectTestCase() {

        @Test
        override fun `test 'run' from release branch should set SNASPSHOT version`() {
            `test 'run' from any branch should set SNASPSHOT version`(scmConfig.releaseBranch)
        }

        @Test
        override fun `test 'run' from feature branch should set SNASPSHOT version`() {
            `test 'run' from any branch should set SNASPSHOT version`(scmConfig.featureBranch)
        }

        private fun `test 'run' from any branch should set SNASPSHOT version`(testBranch: String) = runTestCase {
            givenTestCase {
                withScmProject()

                scmActions.checkout(projectDir.localProjectFile, testBranch)

                setProjectVersion(projectDir.localProjectFile, RELEASE_001)
                scmActions.add(projectDir.localProjectFile, ".")
                scmActions.commit(projectDir.localProjectFile, "Release version: $RELEASE_001")
                scmActions.push(projectDir.localProjectFile, scmConfig.remote, testBranch)
            }

            whenExecute {
                gradleTaskSucceeds(SET_SNAPSHOT_VERSION_TASK_NAME)
            }

            thenVerify {
                gradleTaskOutput {
                    contains("> Task :$SET_SNAPSHOT_VERSION_TASK_NAME")
                }

                scmStatusIn(projectDir.localProjectFile) {
                    contains(
                        "On branch ${scmConfig.featureBranch}",
                        "Your branch is up to date with '${scmConfig.remote}/${scmConfig.featureBranch}'.",
                        "Changes not staged for commit:",
                        "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                        "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                    )
                }

                projectVersionIn(projectDir.localProjectFile) {
                    isEqualTo(SNAPSHOT_002)
                }
            }
        }

    }

    @Nested
    inner class SingleModuleCustomizedTestCase :
        SetSnapshotVersionTaskSingleModuleTestCase,
        SingleModuleCustomizedScmProjectTestCase()

    @Nested
    inner class MultiModuleGitFlowTestCase :
        SetSnapshotVersionTaskMultiModuleTestCase,
        MultiModuleGitFlowScmProjectTestCase()

    @Nested
    inner class MultiModuleTrunkFlowTestCase :
        SetSnapshotVersionTaskMultiModuleTestCase,
        MultiModuleTrunkFlowScmProjectTestCase() {

        @Test
        override fun `test 'run' from release branch should set SNASPSHOT version`() = runTestCase {
            givenTestCase {
                withScmProject()

                scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)

                allProjectDirs.forEach {
                    setProjectVersion(it.localProjectFile, RELEASE_001)
                    scmActions.add(it.localProjectFile, ".")
                    scmActions.commit(it.localProjectFile, "Release version: $RELEASE_001")
                    scmActions.push(it.localProjectFile, scmConfig.remote, scmConfig.releaseBranch)
                }
            }

            whenExecute {
                gradleTaskSucceeds(SET_SNAPSHOT_VERSION_TASK_NAME)
            }

            thenVerify {
                gradleTaskOutput {
                    contains("> Task :$SET_SNAPSHOT_VERSION_TASK_NAME")
                }

                with(projectDir.localProjectFile) {
                    scmStatusIn(this) {
                        contains(
                            "On branch ${scmConfig.featureBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.featureBranch}'.",
                            "Changes not staged for commit:",
                            *SUBMODULE_NAMES.map { "modified:   $it (modified content)" }
                                .toTypedArray(),
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                            "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                        )
                    }

                    projectVersionIn(this) {
                        isEqualTo(SNAPSHOT_002)
                    }
                }

                submoduleProjectDirs.forEach {
                    scmStatusIn(it.localProjectFile) {
                        contains(
                            "On branch ${scmConfig.featureBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.featureBranch}'.",
                            "Changes not staged for commit:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                            "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                        )
                    }

                    projectVersionIn(it.localProjectFile) {
                        isEqualTo(SNAPSHOT_002)
                    }
                }
            }
        }

    }

    @Nested
    inner class MultiModuleCustomizedTestCase :
        SetSnapshotVersionTaskMultiModuleTestCase,
        MultiModuleCustomizedScmProjectTestCase()

}
