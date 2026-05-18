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
        namespace = "com.moly3.cedarjam.pages.page_workspace"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    jvm()

    listOf(iosArm64(), iosSimulatorArm64())
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.navigation)
            implementation(projects.core.ui)
            implementation(projects.core.coordinator)


            implementation(projects.features.featureSettings)

            implementation(projects.pages.pageTabs)

            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)

            implementation(libs.decompose.compose)
            implementation(libs.colorpicker.compose)
        }
    }
}
