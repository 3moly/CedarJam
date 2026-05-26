@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.serialization)
    alias(libs.plugins.metro)
    alias(libs.plugins.kover)
}

kover {
    currentProject {
        createVariant("custom") {
            add("jvm")
        }
    }
}

kotlin {

    android {
        namespace = "com.moly3.cedarjam.data"
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
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.domain)



                implementation(libs.shared.logger.kermit)
                implementation(libs.coroutines)

                implementation(libs.datetime)
                implementation(libs.serialization)

                implementation(libs.ktor)
                implementation(libs.ktor.serialization)
                implementation(libs.ktor.contentnegotiation)
                implementation(libs.ktor.logging)
                implementation(libs.ktor.json)
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)

        }
        jvmMain.dependencies {
            implementation(libs.ktor.cio)
        }
        androidMain.dependencies {
            implementation(libs.ktor.cio)
        }
        iosMain.dependencies {
            implementation(libs.ktor.darwin)
        }
    }
}
