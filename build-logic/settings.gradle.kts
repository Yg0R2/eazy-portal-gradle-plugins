rootProject.name = "build-logic"

include("conventions")
project(":conventions").apply {
    name = "build-logic-conventions"
    projectDir = file("../conventions")
}
