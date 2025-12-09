package org.eazyportal.plugin.gradle.release.asd

class Qwe2 {

    interface BaseTestCase

    // --- Core fluent API with minimal generics ---

    abstract class BaseGiven<P : BaseTestCase, SELF : BaseGiven<P, SELF>> {

        abstract fun createWhen(): BaseWhen<P, *>

        fun givenSomething(): BaseWhen<P, *> = createWhen()
    }

    abstract class BaseWhen<P : BaseTestCase, SELF : BaseWhen<P, SELF>> {

        abstract fun createThen(): BaseThen<P>

        fun thenDoSomething(): BaseThen<P> = createThen()
    }

    open class BaseThen<P : BaseTestCase> {
        fun thenValidate(): BaseThen<P> =
            also { println("[HERE] ${this::class.simpleName}") }
    }

    // --- SCM-specific test case marker ---

    interface ScmBaseTestCase : BaseTestCase

    // --- SCM family: no overrides needed for chaining return types ---

    class ScmBaseGiven<P : ScmBaseTestCase> :
        BaseGiven<P, ScmBaseGiven<P>>() {

        override fun createWhen(): BaseWhen<P, *> =
            ScmBaseWhen()
    }

    class ScmBaseWhen<P : ScmBaseTestCase> :
        BaseWhen<P, ScmBaseWhen<P>>() {

        override fun createThen(): BaseThen<P> =
            ScmBaseThen()
    }

    class ScmBaseThen<P : ScmBaseTestCase> : BaseThen<P>()
}

fun main() {
//    println("[HERE]")
//    Qwe2.BaseGiven<Qwe2.BaseTestCase, *>().givenSomething().thenDoSomething().thenValidate()
//    Qwe2.BaseWhen<Qwe2.BaseTestCase, *>().thenDoSomething().thenValidate()
//    Qwe2.BaseThen<Qwe2.BaseTestCase>().thenValidate()
//
//    println("[HERE]")
//    Qwe2.ScmBaseGiven<Qwe2.ScmBaseTestCase>().givenSomething().thenDoSomething().thenValidate()
//    Qwe2.ScmBaseWhen<Qwe2.ScmBaseTestCase>().thenDoSomething().thenValidate()
//    Qwe2.ScmBaseThen<Qwe2.ScmBaseTestCase>().thenValidate()
//    println("[HERE]")
}
