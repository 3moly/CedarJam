@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    android {
        namespace = "com.moly3.cedarjam.domain"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
//        defaultConfig {
//            minSdk = libs.versions.android.minSdk.get().toInt()
//        }
//        compileOptions {
//            sourceCompatibility = JavaVersion.VERSION_17
//            targetCompatibility = JavaVersion.VERSION_17
//        }
    }
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
                api(libs.compose.components.resources)

                implementation(libs.compose.data.viz.core)

                //planning to remove soon
                implementation(libs.compose.foundation)
                implementation(libs.filekit.core)
                implementation(libs.filekit.dialogs)
                api(libs.okio)
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

