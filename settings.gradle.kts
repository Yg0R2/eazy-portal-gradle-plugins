rootProject.name = "gradle-plugins"

includeBuild("conventions")

listOf(
    "project",
    "settings",
).forEach {
    include("portal-$it")
    project(":portal-$it").projectDir = file("./portal/$it")
}
