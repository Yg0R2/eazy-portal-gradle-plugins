rootProject.name = "gradle-plugins"

pluginManagement {
    includeBuild("build-logic")
    includeBuild("conventions")
}

listOf(
    "common",
    "project",
    "settings",
).forEach {
    include(it)
    project(":$it").projectDir = file("./portal/$it")
}
