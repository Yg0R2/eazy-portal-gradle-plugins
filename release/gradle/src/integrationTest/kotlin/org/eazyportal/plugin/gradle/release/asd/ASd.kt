package org.eazyportal.plugin.gradle.release.asd

class Asd {
    interface BaseTestCase

    interface ScmBaseTestCase : BaseTestCase

    open class BaseWhen<T : BaseTestCase>

    open class BaseGiven<T : BaseTestCase, W : BaseWhen<T>> {
        @Suppress("UNCHECKED_CAST")
        open fun whenSomething(): BaseWhen<T> =
            BaseWhen()
    }

    class ScmWhen<T : ScmBaseTestCase> : BaseWhen<T>()

    class ScmGiven<T : ScmBaseTestCase> : BaseGiven<T, ScmWhen<T>>() {
        override fun whenSomething(): ScmWhen<T> =
            ScmWhen()
    }
}

class Qwe {
    interface BaseTestCase

    open class BaseGiven<out P : BaseTestCase, out W : BaseWhen<P, BaseThen<P>>> {
        @Suppress("UNCHECKED_CAST")
        open fun givenSomething(): W =
            BaseWhen<P, BaseThen<P>>() as W
    }

    open class BaseWhen<out P : BaseTestCase, out T: BaseThen<P>> {
        @Suppress("UNCHECKED_CAST")
        open fun thenDoSomething(): T =
            BaseThen<P>() as T
    }

    open class BaseThen<out P: BaseTestCase> {
        fun thenValidate(): BaseThen<P> =
            also { println("[HERE] ${it::class.simpleName}") }
    }


    interface ScmBaseTestCase : BaseTestCase

    class ScmBaseGiven<P : ScmBaseTestCase, W : ScmBaseWhen<P, ScmBaseThen<P>>> : BaseGiven<P, W>() {
        @Suppress("UNCHECKED_CAST")
        override fun givenSomething(): W =
            ScmBaseWhen<P, ScmBaseThen<P>>() as W
    }

    class ScmBaseWhen<P: ScmBaseTestCase, T: ScmBaseThen<P>> : BaseWhen<P, T>() {
        @Suppress("UNCHECKED_CAST")
        override fun thenDoSomething(): T =
            ScmBaseThen<P>() as T
    }

    class ScmBaseThen<P: ScmBaseTestCase> : BaseThen<P>()

}

class Xyz {
    interface BaseTestCase<out P : BaseTestCase<P>> {
        fun giveTestCase(): Given<P, *>
    }

    class BaseProjectTestCase : BaseTestCase<BaseProjectTestCase> {

        override fun giveTestCase(): BaseGiven<BaseProjectTestCase> =
            BaseGiven()

    }

    abstract class Given<out P : Any, SELF : Given<P, SELF>> {
        abstract fun givenSomething(): When<P, *>
    }

    abstract class When<out P : Any, SELF : When<P, SELF>> {
        abstract fun thenDoSomething(): Then<P, *>
    }

    abstract class Then<out P: Any, SELF : Then<P, SELF>> {
        fun thenValidate(): SELF =
            (this as SELF).also { println("[HERE] ${it::class.simpleName}") }
    }


    open class BaseGiven<P : BaseTestCase<P>> : Given<P, BaseGiven<P>>() {
        override fun givenSomething(): BaseWhen<P> =
            BaseWhen()
    }

    open class BaseWhen<P : BaseTestCase<P>> : When<P, BaseWhen<P>>() {
        override fun thenDoSomething(): BaseThen<P> =
            BaseThen()
    }

    open class BaseThen<P: BaseTestCase<P>> : Then<P, BaseThen<P>>() {
    }


    interface ScmBaseTestCase<P : ScmBaseTestCase<P>> : BaseTestCase<P> {
        override fun giveTestCase(): ScmBaseGiven<P>
    }

    class ScmProjectTestCase : ScmBaseTestCase<ScmProjectTestCase> {
        override fun giveTestCase(): ScmBaseGiven<ScmProjectTestCase> =
            ScmBaseGiven()
    }

    class ScmBaseGiven<P : ScmBaseTestCase<P>> : BaseGiven<P>() {
        override fun givenSomething(): ScmBaseWhen<P> =
            ScmBaseWhen()
    }

    class ScmBaseWhen<P: ScmBaseTestCase<P>> : BaseWhen<P>() {
        override fun thenDoSomething(): ScmBaseThen<P> =
            ScmBaseThen()
    }

    class ScmBaseThen<P: ScmBaseTestCase<P>> : BaseThen<P>() {
        fun asd() {
            println("[HERE] asd")
        }
    }

}

fun main() {
//    println("[HERE]")
//    Qwe.BaseGiven<Qwe.BaseTestCase, Qwe.BaseWhen<Qwe.BaseTestCase, Qwe.BaseThen<Qwe.BaseTestCase>>>().givenSomething().thenDoSomething().thenValidate()
//    Qwe.BaseWhen<Qwe.BaseTestCase, Qwe.BaseThen<Qwe.BaseTestCase>>().thenDoSomething().thenValidate()
//    Qwe.BaseThen<Qwe.BaseTestCase>().thenValidate()
//    println("[HERE]")
//    Qwe.ScmBaseGiven<Qwe.ScmBaseTestCase, Qwe.ScmBaseWhen<Qwe.ScmBaseTestCase, Qwe.ScmBaseThen<Qwe.ScmBaseTestCase>>>().givenSomething().thenDoSomething().thenValidate()
//    Qwe.ScmBaseWhen<Qwe.ScmBaseTestCase, Qwe.ScmBaseThen<Qwe.ScmBaseTestCase>>().thenDoSomething().thenValidate()
//    Qwe.ScmBaseThen<Qwe.ScmBaseTestCase>().thenValidate()
//    println("[HERE]")

    println("[HERE]")
//    val baseGiven: Xyz.BaseGiven<Xyz.BaseTestCase, *> = Xyz.BaseGiven()
    val baseGiven = Xyz.BaseProjectTestCase().giveTestCase()
    val baseWhen = baseGiven.givenSomething()
    val baseThen = baseWhen.thenDoSomething()
    baseThen.thenValidate()

    println("[HERE]")
    val scmBaseGiven= Xyz.ScmProjectTestCase().giveTestCase()
    val scmBaseWhen = scmBaseGiven.givenSomething()
    val scmBaseThen = scmBaseWhen.thenDoSomething()
//    scmBaseThen.thenValidate().asd()
    scmBaseThen.asd()
    println("[HERE]")
    Xyz.ScmProjectTestCase().giveTestCase().givenSomething().thenDoSomething().asd()
    println("[HERE]")
}

