plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.zktony.www"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.zktony.www"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".liquid.four.debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".liquid.four.release"
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    signingConfigs {
        named("debug") {
            storeFile = rootProject.file("zktony.keystore")
            storePassword = "zktony"
            keyAlias = "zktony"
            keyPassword = "zktony"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        jniLibs.keepDebugSymbols += listOf(
            "*/x86/*.so",
            "*/x86_64/*.so",
            "*/armeabi-v7a/*.so",
            "*/arm64-v8a/*.so"
        )
    }

    buildFeatures {
        dataBinding = true
    }

    applicationVariants.all {
        outputs.all {
            (this as? com.android.build.gradle.internal.api.ApkVariantOutputImpl)?.outputFileName =
                "zktony-liquid-four-${versionName}-${name}.apk"
        }
    }
}

dependencies {
    ksp(libs.androidx.room.compiler)
    implementation(project(mapOf("path" to ":Lib_Core")))
    implementation(project(mapOf("path" to ":Lib_SerialPort")))

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.datastore.preferences)
    implementation(libs.dialogx)
    implementation(libs.gson)
    implementation(libs.koin.android)
    implementation(libs.material)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.ext.junit)

    testImplementation(libs.junit)
}