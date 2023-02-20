@file:Suppress("UnstableApiUsage")

include(":common")

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

rootProject.name = "ZkTony-App"
include(":ZkTony_FY")
include(":ZkTony_LIQUID")
include(":ZkTony_TEST")
include(":ZkTony_ZM")
include(":gpio")
include(":serialport")

