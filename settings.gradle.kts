rootProject.name = "gradle-plugins"

includeBuild("conventions")

listOf(
    "common",
    "project",
    "settings",
).forEach {
    include("portal-$it")
    project(":portal-$it").projectDir = file("./portal/$it")
}
