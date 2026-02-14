plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    
}

kotlin {
    android {
        namespace = "com.moly3.cedarjam.pages.page_tabs"
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
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.navigation)
            implementation(projects.core.ui)

            implementation(libs.decompose.compose)
            implementation(libs.decompose.compose.experimental)
            
            implementation(projects.pages.pageTab)

        }
    }
}
