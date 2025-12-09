//package org.eazyportal.plugin.gradle.release.testcase
//
//import org.assertj.core.api.ListAssert
//import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
//import org.eazyportal.plugin.common.integration.test.gradle.GradleUtils.createGradleRunner
//import org.eazyportal.plugin.common.integration.test.testcase.BaseProjectGiven
//import org.eazyportal.plugin.common.integration.test.testcase.BaseProjectThen
//import org.eazyportal.plugin.common.integration.test.testcase.BaseProjectWhen
//import org.gradle.testkit.runner.BuildResult
//import org.gradle.testkit.runner.GradleRunner
//
//class ScmProjectGiven<P : BaseScmProjectTestCase>(
//    private val testCase: P,
//    initProjectBlock: GradleProjectBuilder.() -> Unit,
//) : BaseProjectGiven<P, ScmProjectWhen<P, ScmProjectThen<P>>>(
//    testCase,
//    initProjectBlock,
//) {
//
//    override fun whenGradleTask(
//        taskName: String,
//        vararg arguments: String,
//        gradleTaskBlock: GradleRunner.() -> BuildResult
//    ): ScmProjectWhen<P> =
//        createGradleRunner(testCase.projectDir, taskName, *arguments)
//            .let(gradleTaskBlock)
//            .let { ScmProjectWhen(testCase, it) }
//
//}
//
//class ScmProjectWhen<T : BaseScmProjectTestCase>(
//    private val testCase: T,
//    private val result: BuildResult,
//) : BaseProjectWhen<T>(
//    testCase,
//    result,
//) {
//
//    override fun thenAssertTaskOutput(block: ListAssert<String>.() -> Unit): BaseProjectThen<T> =
//        BaseProjectThen(testCase, result)
//            .also { it.thenAssertTaskOutput(block) }
//
//
//}
//
//class ScmProjectThen<T : BaseScmProjectTestCase>(
//    private val testCase: T,
//    private val buildResult: BuildResult,
//) : BaseProjectThen<T>(
//    testCase,
//    buildResult,
//) {
//
////    fun thenScmAssert(block: ScmAssert.() -> Unit) {
////
////    }
//
////    class ScmAssert(
////        private val testCase: BaseScmProjectTestCase,
////    ) {
////
////        fun commitsIn(
////            projectFile: ProjectFile<File>,
////            block: ListAssert<String>.() -> Unit,
////        ) {
////            block(assertThat(testCase.scmActions.getCommits(projectFile)))
////        }
////
////        fun statusIn(
////            projectFile: ProjectFile<File>,
////            block: ListAssert<String>.() -> Unit,
////        ) {
////            block(assertThat(testCase.scmActions.status(projectFile)))
////        }
////
////        fun statusCleanIn(
////            projectFile: ProjectFile<File>,
////            branch: String,
////        ) {
////            statusIn(projectFile) {
////                containsExactly(
////                    "On branch $branch",
////                    "Your branch is up to date with '${testCase.scmConfig.remote}/$branch'.",
////                    "nothing to commit, working tree clean",
////                )
////            }
////        }
////
////    }
//
//}
