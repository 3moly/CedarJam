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

            implementation(libs.compose.material3)
            implementation(libs.compose.material)
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.ui)
            api(libs.compose.ui.tooling.preview)
            api(libs.compose.components.resources)

            api(libs.hypnoticcanvas)
            api(libs.hypnoticcanvas.shaders)
            api(libs.haze)

            api(libs.coil.compose)
            implementation(libs.videoplayer)
            api(libs.dnd)
            api(libs.lazytable)
            api(libs.localina)
        }
        commonTest.dependencies{
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            api(libs.jewel.decorated.window)
            implementation(libs.pdfbox)
        }
    }
}
dependencies {
    api(libs.compose.ui.tooling)
}
compose.resources {
    publicResClass = true
    packageOfResClass = "com.moly3.cedarjam.ui"
    generateResClass = auto
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