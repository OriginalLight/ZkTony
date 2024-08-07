plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.zktony.log"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    lint {
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.android)

    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.ext.junit)

    testImplementation(libs.junit)
}