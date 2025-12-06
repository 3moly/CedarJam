@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

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
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    )

    wasmJs {
        browser()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.domain)
                implementation(projects.core.net)
                implementation(projects.core.storage)
                implementation(libs.coroutines)
                implementation(libs.datetime)
                implementation(libs.foundation)
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }
    }
}
android {
    namespace = "com.moly3.cedarjam.storage"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    buildFeatures.compose = true
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}