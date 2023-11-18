rootProject.name = "fastmc-texture"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.luna5ama.dev/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://files.minecraftforge.net/maven/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }
}

//includeBuild("../mod-setup")
//includeBuild("../fastmc-common") {
//    dependencySubstitution {
//        substitute(module("dev.fastmc:fastmc-common")).using(project(":"))
//        substitute(module("dev.fastmc:fastmc-common-java8")).using(project(":java8"))
//        substitute(module("dev.fastmc:fastmc-common-java17")).using(project(":java17"))
//    }
//}

include("shared")
include("shared:java8")
include("forge-1.12.2")