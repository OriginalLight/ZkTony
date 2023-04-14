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
include(":ZkTony_FY")
include(":ZkTony_LIQUID_FOUR")
include(":ZkTony_MIX_AUTO")
include(":ZkTony_MIX_MANUAL")
include(":ZkTony_TEST")
include(":ZkTony_ZM")
