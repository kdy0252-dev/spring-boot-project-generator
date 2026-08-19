tasks.register("buildAll") {
    group = "build"
    description = "Builds every application module."
    dependsOn(":app:build")
}
