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
                implementation(projects.core.domain)

                api(libs.decompose)
                api(libs.decompose.compose.experimental)
                api(libs.mvi)
                api(libs.mvi.coroutines)
                api(libs.mvi.kotlin)
                api(libs.koin)
                api(libs.coroutines)

                implementation(libs.compose.foundation)
            }
        }
    }
}
android {
    namespace = "com.moly3.cedarjam.navigation"
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