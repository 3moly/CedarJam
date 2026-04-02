plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.metro)
}

kotlin {
    android {
        namespace = "com.moly3.cedarjam.navigation"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
    }
    jvm()
    listOf(iosArm64(), iosSimulatorArm64())
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
