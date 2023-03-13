@file:Suppress("UnstableApiUsage")

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
include(":ZkTony_LIQUID_FOUR")
include(":ZkTony_TEST")
include(":ZkTony_ZM")
include(":ZkTony_MIX_AUTO")
include(":common")
include(":gpio")
include(":serialport")

