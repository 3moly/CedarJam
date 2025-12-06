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

            implementation(libs.material3)
            implementation(libs.material)
            api(libs.runtime)
            api(libs.foundation)
            api(libs.ui)
            api(libs.ui.tooling.preview)
            api("org.jetbrains.compose.components:components-resources:1.10.0-rc01")

            api(libs.hypnoticcanvas)
            api(libs.hypnoticcanvas.shaders)
            api(libs.haze)

            api(libs.coil.compose)
            implementation(libs.videoplayer)
            api(libs.dnd)
            api(libs.lazytable)
            api("io.github.sudarshanmhasrup.localina:localina:1.0.0-alpha3")
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
    api("org.jetbrains.compose.ui:ui-tooling:1.10.0-rc01")
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