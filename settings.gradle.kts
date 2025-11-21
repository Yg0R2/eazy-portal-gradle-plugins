rootProject.name = "gradle-plugins"

pluginManagement {
    includeBuild("build-logic")
    includeBuild("conventions")
}

include("common")

listOf(
    "common",
    "project",
    "settings",
).forEach {
    include("portal-$it")
    project(":portal-$it").projectDir = file("./portal/$it")
}

listOf(
    "core",
    "gradle",
).forEach {
    include("release-$it")
    project(":release-$it").projectDir = file("./release/$it")
}
