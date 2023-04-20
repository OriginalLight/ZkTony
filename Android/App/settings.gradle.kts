pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "App"
include(":core")
include(":datastore")
include(":gpio")
include(":protobuf")
include(":room")
include(":serialport")
include(":Incubation")
include(":Liquid_Four")
include(":Liquid_One")
include(":Mix_Auto")
include(":Mix_Manual")
include(":Test")
include(":Transfer")
