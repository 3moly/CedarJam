plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
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
        namespace = "com.moly3.cedarjam.navigation"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
    }
    jvm()
    listOf(iosArm64(), iosSimulatorArm64())
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.domain)
                implementation(projects.core.coordinator)
                implementation(projects.core.ui)

                api(libs.essenty.keeper)
                api(libs.decompose)
                api(libs.decompose.compose.experimental)
                api(libs.mvi)
                api(libs.mvi.coroutines)
                api(libs.mvi.kotlin)
                api(libs.coroutines)

                implementation(libs.compose.foundation)
            }
        }
    }
}
