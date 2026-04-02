@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.serialization)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.metro)
}

kotlin {

    android {
        namespace = "com.moly3.cedarjam.core.storage"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
    }
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
                implementation(libs.compose.foundation)
                implementation(libs.sqldelight.extensions)
                implementation(libs.compose.data.viz)

                implementation(libs.kmp.io)
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.jvm)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.ios)
        }
        jvmMain.dependencies {}
    }
}

sqldelight {
    linkSqlite.set(true)
    databases {
        create("Database") {
            packageName.set("com.moly3.cedarjam.db")
            srcDirs("src/commonMain/sqldelight/maindb")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases/schemas"))
        }
        create("IndexDatabase") {
            packageName.set("com.moly3.cedarjam.indexdb")
            srcDirs("src/commonMain/sqldelight/indexdb")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases/schemas"))
        }
    }
}