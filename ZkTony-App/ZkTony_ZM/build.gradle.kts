@file:Suppress("UnstableApiUsage")
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kapt)
    alias(libs.plugins.safeargs)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.zktony.www"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.zktony.www"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 10
        versionName = "1.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["dagger.hilt.disableModulesHaveInstallInCheck"] = "true"
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".zm.debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".zm.release"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

    packagingOptions {
        jniLibs.keepDebugSymbols += listOf(
            "*/x86/*.so",
            "*/x86_64/*.so",
            "*/armeabi-v7a/*.so",
            "*/arm64-v8a/*.so"
        )
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }

    buildFeatures {
        dataBinding = true
    }

    applicationVariants.all {
        outputs.all {
            (this as? com.android.build.gradle.internal.api.ApkVariantOutputImpl)?.outputFileName =
                "zktony-zm-${versionName}-${name}.apk"
        }
    }
}

dependencies {
    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar", "*.aar")))
    implementation(project(mapOf("path" to ":common")))
    implementation(project(mapOf("path" to ":gpio")))
    implementation(project(mapOf("path" to ":serialport")))
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
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.chart)
    implementation(libs.datastore.preferences)
    implementation(libs.dialogx)
    implementation(libs.gson)
    implementation(libs.hilt.android)
    implementation(libs.hilt.work)
    implementation(libs.material)
    implementation(libs.okhttp3)
    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.retrofit2)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.zxing)

    kapt(libs.androidx.room.compiler)
    kapt(libs.hilt.android.compiler)
    kapt(libs.hilt.compiler)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
}