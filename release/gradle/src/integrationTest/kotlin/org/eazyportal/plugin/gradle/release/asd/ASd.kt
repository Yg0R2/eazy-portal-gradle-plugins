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
    interface BaseTestCase

    abstract class Given<out P : Any, SELF : Given<P, SELF>> {
        abstract fun givenSomething(): When<P, *>
    }

    abstract class When<out P : Any, SELF : When<P, SELF>> {
        abstract fun thenDoSomething(): Then<P>
    }

    abstract class Then<out P: Any> {
        abstract fun thenValidate(): Then<P>
    }


    open class BaseGiven<P : BaseTestCase> : Given<P, BaseGiven<P>>() {
        override fun givenSomething(): BaseWhen<P> =
            BaseWhen()
    }

    open class BaseWhen<P : BaseTestCase> : When<P, BaseWhen<P>>() {
        override fun thenDoSomething(): BaseThen<P> =
            BaseThen()
    }

    open class BaseThen<P: BaseTestCase> : Then<P>() {
        override fun thenValidate(): BaseThen<P> =
            also { println("[HERE] ${it::class.simpleName}") }
    }


    interface ScmBaseTestCase : BaseTestCase

    class ScmBaseGiven<P : ScmBaseTestCase> : BaseGiven<P>() {
        @Suppress("UNCHECKED_CAST")
        override fun givenSomething(): ScmBaseWhen<P> =
            ScmBaseWhen()
    }

    class ScmBaseWhen<P: ScmBaseTestCase> : BaseWhen<P>() {
        @Suppress("UNCHECKED_CAST")
        override fun thenDoSomething(): ScmBaseThen<P> =
            ScmBaseThen()
    }

    class ScmBaseThen<P: ScmBaseTestCase> : BaseThen<P>()

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
    val baseGiven = Xyz.BaseGiven<Xyz.BaseTestCase>()
    val baseWhen = baseGiven.givenSomething()
    val baseThen = baseWhen.thenDoSomething()
    baseThen.thenValidate()

    println("[HERE]")
    val scmBaseGiven= Xyz.ScmBaseGiven<Xyz.ScmBaseTestCase>()
    val scmBaseWhen = scmBaseGiven.givenSomething()
    val scmBaseThen = scmBaseWhen.thenDoSomething()
    scmBaseThen.thenValidate()
    println("[HERE]")
}

