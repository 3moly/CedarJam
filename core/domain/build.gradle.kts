@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget()
    jvm()
    listOf(iosArm64(), iosSimulatorArm64())
    wasmJs {
        browser()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.datetime)
                api(libs.kotlinx.io.core)
                api(libs.kotlinx.immutable.list)
                api(libs.shared.logger.kermit)
                implementation(libs.coroutines)
                api(libs.serialization)

                implementation(libs.compose.data.viz.core)

                //planning to remove soon
                implementation(libs.foundation)
                implementation(libs.filekit.core)
                implementation(libs.filekit.dialogs)
            }
        }
        commonTest.dependencies{
            implementation(libs.kotlin.test)
        }
        androidUnitTest.dependencies {
            implementation(libs.robolectric)
        }
    }
    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
}
android {
    namespace = "com.moly3.cedarjam.domain"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}
val mySyncServerUrl = localProperties.getProperty("cedarjam_server.url") ?: ""

// Register cache-safe task
abstract class GenerateBuildConfigTask : DefaultTask() {
    @get:Input
    abstract val syncServerUrl: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val outputFile = outputDir.get().file("BuildConfig.kt").asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            """
            package com.moly3.core_domain

            object BuildConfig {
                const val SyncServerUrl = "${syncServerUrl.get()}"
            }
            """.trimIndent()
        )
    }
}

val generateBuildConfig by tasks.registering(GenerateBuildConfigTask::class) {
    syncServerUrl.set(mySyncServerUrl)
    outputDir.set(layout.buildDirectory.dir("generated/source/buildConfig/commonMain/kotlin"))
}

// Add generated source to the commonMain source set
kotlin.sourceSets.named("commonMain") {
    kotlin.srcDir(generateBuildConfig.map { it.outputDir })
}