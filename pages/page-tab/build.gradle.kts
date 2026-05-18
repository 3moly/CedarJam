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
        namespace = "com.moly3.cedarjam.pages.page_tab"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
    }
    jvm()

    listOf(iosArm64(), iosSimulatorArm64())
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.navigation)
            implementation(projects.core.ui)

            implementation(projects.features.featureGraph)
            implementation(projects.pages.pageHome)
            implementation(projects.pages.pageFile)
            implementation(projects.pages.pageCollection)
            implementation(projects.pages.pageCollectionRow)
            implementation(projects.pages.pageGraph)
            implementation(projects.pages.pageTags)
            implementation(projects.pages.pageTag)

            implementation(libs.decompose.compose)
            implementation(libs.decompose.compose.experimental)
            implementation(libs.constraintlayout.compose.multiplatform)
        }
    }
}
