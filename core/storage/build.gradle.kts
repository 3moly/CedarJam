@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.serialization)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
//    kotlin("native.cocoapods") // Add this plugin
}

kotlin {

    androidTarget()
    jvm()
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "CoreData"
            isStatic = true
            linkerOpts("-lsqlite3")
        }
    }
//    cocoapods {
//        summary = "Child module with zip support"
//        homepage = "https://github.com/yourproject"
//        version = "1.0"
//        ios.deploymentTarget = "16.0"
//        framework {
//            baseName = "CoreData"
//        }
//
//        pod("SSZipArchive") {
//            version = "~> 2.5"  // Use 2.5 instead of 2.6
//        }
//    }

    wasmJs {
        browser()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.domain)

                implementation(libs.koin)

                implementation(libs.shared.logger.kermit)
                implementation(libs.coroutines)

                implementation(libs.datetime)
                implementation(libs.serialization)
                implementation(libs.key.value.settings.noarg)
                implementation(libs.key.value.settings.serialization)
                implementation(libs.key.value.settings.test)
                implementation(libs.kotlinx.io.core)
                implementation(libs.filekit.core)
                implementation(compose.foundation)
                implementation(libs.sqldelight.extensions)
                implementation(libs.compose.data.viz)


            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)

        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.jvm)
            implementation(libs.kmp.io)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android)
            implementation(libs.kmp.io)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.ios)
            implementation(libs.kmp.io)
        }
        jvmMain.dependencies {
            implementation(libs.kmp.io)
        }
    }
}
android {
    namespace = "com.moly3.cedarjam.core.storage"
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
sqldelight {
    linkSqlite.set(true)
    databases {
        create("Database") {
            packageName.set("com.moly3.cedarjam.core.storage")
        }
    }
}