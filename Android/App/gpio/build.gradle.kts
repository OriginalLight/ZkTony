plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.zktony.gpio"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        ndk {
            abiFilters += listOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
        }

        ndkVersion = libs.versions.ndk.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    externalNativeBuild {
        cmake {
            path = file("CMakeLists.txt")
            version = libs.versions.cmake.get()
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}