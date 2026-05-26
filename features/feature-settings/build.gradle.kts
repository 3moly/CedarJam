
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
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
        namespace = "com.moly3.cedarjam.features.feature_settings"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
//        defaultConfig {
//            minSdk = libs.versions.android.minSdk.get().toInt()
//        }
//        buildFeatures.compose = true
//        compileOptions {
//            sourceCompatibility = JavaVersion.VERSION_17
//            targetCompatibility = JavaVersion.VERSION_17
//        }
    }
    jvm()
    listOf(iosArm64(), iosSimulatorArm64())
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.domain)
                implementation(projects.core.navigation)
                implementation(projects.core.ui)
                implementation(libs.decompose.compose.experimental)

                implementation(libs.shared.logger.kermit)
                implementation(libs.compose.foundation)
                implementation(libs.filekit.dialogs)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
