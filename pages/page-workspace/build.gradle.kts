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
        commonMain.dependencies {
            //TODO remove
//            implementation(projects.core.storage)

            implementation(projects.core.domain)
            implementation(projects.core.navigation)
            implementation(projects.core.ui)
            implementation(projects.pages.pageTabs)
            implementation(projects.features.featureSettings)

            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)

            implementation(libs.decompose.compose)
            implementation(libs.colorpicker.compose)
        }
    }
}
android {
    namespace = "com.moly3.cedarjam.pages.page_workspace"
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