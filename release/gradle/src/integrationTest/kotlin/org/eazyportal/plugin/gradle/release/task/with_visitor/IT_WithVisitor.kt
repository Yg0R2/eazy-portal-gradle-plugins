package org.eazyportal.plugin.gradle.release.task.with_visitor

import org.junit.jupiter.api.Test

interface TestCase {
    fun accept(visitor: TestCaseVisitor)
}

interface TestCaseVisitor {
    fun visit(test: SetReleaseVersionTaskTestCase)
    // and ALL other test cases
    fun visit(test: FinalizeReleaseVersionTaskTestCase)
}

interface SetReleaseVersionTaskTestCase : TestCase {

    fun `test 'run' should fail when there are no acceptable commits`()

    fun `test 'run' should succeed when there are acceptable commits on release`()

    fun `test 'run' should succeed when there are acceptable commits on feature`()

}

class SetReleaseVersionTaskIT : SetReleaseVersionTaskTestCase, BaseSingleModuleIT() {

    private lateinit var testCaseVisitor: TestCaseVisitor

    @Test
    override fun `test 'run' should fail when there are no acceptable commits`() {
        println(testCaseVisitor::class)
    }

    override fun `test 'run' should succeed when there are acceptable commits on release`() {
        TODO("Not yet implemented")
    }

    override fun `test 'run' should succeed when there are acceptable commits on feature`() {
        TODO("Not yet implemented")
    }

    override fun accept(visitor: TestCaseVisitor) {
        testCaseVisitor = visitor
        visitor.visit(this)
    }

}

interface FinalizeReleaseVersionTaskTestCase : TestCase {

    fun `test 'run' should finalize`()

}


class FinalizeReleaseVersionTaskIT : FinalizeReleaseVersionTaskTestCase, BaseSingleModuleIT() {

    private lateinit var testCaseVisitor: TestCaseVisitor

    @Test
    override fun `test 'run' should finalize`() {
        println(testCaseVisitor::class)
    }

    override fun accept(visitor: TestCaseVisitor) {
        testCaseVisitor = visitor

        visitor.visit(this)
    }

}

abstract class BaseSingleModuleIT :
//    BaseITSetup(),
    TestCaseVisitor {

    override fun visit(test: SetReleaseVersionTaskTestCase) {
    }

    override fun visit(test: FinalizeReleaseVersionTaskTestCase) {
    }

}