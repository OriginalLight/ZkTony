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
include(":App_Mix")
include(":App_Incubation")
include(":App_Liquid")
include(":App_Petridish")
include(":Lib_SerialPort")
include(":App_Incubation_Test")
