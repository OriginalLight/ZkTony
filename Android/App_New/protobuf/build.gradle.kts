import com.google.protobuf.gradle.id

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.protobuf)
}

android {
    namespace = "com.zktony.protobuf"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}


protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    plugins {
        id("java") {
            artifact = libs.grpc.gen.java.get().toString()
        }
        id("grpc") {
            artifact = libs.grpc.gen.java.get().toString()
        }
        id("grpckt") {
            artifact = libs.grpc.gen.kotlin.get().toString()
        }
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("kotlin") {
                    option("lite")
                }
            }
            task.plugins {
                id("java") {
                    option("lite")
                }
                id("grpc") {
                    option("lite")
                }
                id("grpckt") {
                    option("lite")
                }
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    api(libs.grpc.stub)
    api(libs.grpc.kotlin.sub)
    api(libs.grpc.protobuf.lite)
    api(libs.protobuf.kotlin.lite)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
}