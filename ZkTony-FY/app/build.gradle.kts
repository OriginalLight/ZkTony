plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs")
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
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        named("debug") {
            storeFile = rootProject.file("zktony.keystore")
            storePassword = "zktony"
            keyAlias = "zktony"
            keyPassword = "zktony"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    packagingOptions {
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
                "zktony-fy-${versionName}-${name}.apk"
        }
    }
}

dependencies {
    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar", "*.aar")))
    implementation(project(mapOf("path" to ":gpio")))
    implementation(project(mapOf("path" to ":serialport")))
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.chart)
    implementation(libs.datastore.preferences)
    implementation(libs.dialogx)
    implementation(libs.eventbus)
    implementation(libs.gson)
    implementation(libs.hilt.android)
    implementation(libs.hilt.work)
    implementation(libs.legacy.support.v4)
    implementation(libs.material)
    implementation(libs.okhttp3)
    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.recyclerview)
    implementation(libs.retrofit2)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.rxandroid)
    implementation(libs.rxjava)
    implementation(libs.androidx.core.ktx)

    kapt(libs.androidx.room.compiler)
    kapt(libs.hilt.android.compiler)
    kapt(libs.hilt.compiler)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
}