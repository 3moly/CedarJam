plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
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
            implementation(projects.core.domain)

            implementation(libs.shared.logger.kermit)
            implementation(libs.coroutines)
            implementation(libs.serialization)

            implementation(compose.material3)
            implementation(compose.material)

            api(compose.runtime)
            api(compose.foundation)
            api(compose.ui)
            api(compose.components.uiToolingPreview)

            api(libs.hypnoticcanvas)
            api(libs.hypnoticcanvas.shaders)
            api(libs.haze)

            api(libs.coil.compose)
            implementation(libs.videoplayer)
            api(libs.dnd)
            api(libs.lazytable)
        }
        commonTest.dependencies{
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(libs.pdfbox)
        }
    }
}
dependencies {
    api(compose.uiTooling)
}
android {
    namespace = "com.moly3.cedarjam.ui"
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