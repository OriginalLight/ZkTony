buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.android.gradle.plugin)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.hilt.android.gradle.plugin)
        classpath(libs.androidx.navigation.safe.args.gradle.plugin)
    }

    val loggerFactory: org.slf4j.ILoggerFactory = org.slf4j.LoggerFactory.getILoggerFactory()
    val addNoOpLogger: java.lang.reflect.Method = loggerFactory.javaClass.getDeclaredMethod("addNoOpLogger", String::class.java)
    addNoOpLogger.isAccessible = true
    addNoOpLogger.invoke(loggerFactory, "com.android.build.api.component.impl.MutableListBackedUpWithListProperty")
    addNoOpLogger.invoke(loggerFactory, "com.android.build.api.component.impl.MutableMapBackedUpWithMapProperty")
}

task("clean", Delete::class) {
    delete = setOf(rootProject.buildDir)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
}