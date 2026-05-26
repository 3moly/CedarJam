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
        namespace = "com.moly3.cedarjam.features.feature_file_view"
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
                implementation(projects.core.ui)
                implementation(projects.features.featureBrowser)
                implementation(libs.videoplayer)
                implementation(libs.serialization)
                api(libs.webview)

                implementation(libs.kotlinx.io.core)
                implementation(libs.filekit.core)

            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        jvmMain.dependencies {
            implementation(libs.pdfbox)
//            implementation(libs.compose.pdf)
            implementation(libs.icepdf.core)
////            implementation(libs.pdfbox)
            implementation(libs.jai.imageio.jpeg2000)

        }
        androidMain.dependencies {
            implementation(libs.compose.pdf)
//            implementation(libs.icepdf.core)
        }
    }
}


