tasks {
    register("listPlugins") {
        val plugins = project.plugins

        doLast {
            plugins.forEach { println(it::class.java.name) }
        }
    }
}
