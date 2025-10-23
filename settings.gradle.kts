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
    include("portal-$it")
    project(":portal-$it").projectDir = file("./portal/$it")
}
