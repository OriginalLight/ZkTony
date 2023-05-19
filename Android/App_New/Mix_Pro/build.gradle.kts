plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.zktony.android"
    defaultConfig {
        applicationId = "com.zktony.android"
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
            applicationIdSuffix = ".mix.pro.debug"
        }

        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".mix.pro.release"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
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

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        // Multiple dependency bring these files in. Exclude them to enable
        // our test APK to build (has no effect on our AARs)

        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }

        jniLibs.keepDebugSymbols += listOf(
            "*/x86/*.so", "*/x86_64/*.so", "*/armeabi-v7a/*.so", "*/arm64-v8a/*.so"
        )

    }

    applicationVariants.all {
        outputs.all {
            (this as? com.android.build.gradle.internal.api.ApkVariantOutputImpl)?.outputFileName =
                "zktony-mix-pro-${versionName}-${name}.apk"
        }
    }

}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(project(mapOf("path" to ":core")))
    implementation(project(mapOf("path" to ":datastore")))
    implementation(project(mapOf("path" to ":protobuf")))
    implementation(project(mapOf("path" to ":serialport")))
    implementation(libs.accompanist.insets)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewModelCompose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.grpc.okhttp)
    implementation(libs.gson)
    implementation(libs.koin.androidx.compose)


    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.ext.junit)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
