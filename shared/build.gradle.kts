@file:OptIn(ExperimentalComposeLibrary::class)

import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.stability.analyzer)
    kotlin("native.cocoapods")
}

kotlin {
    // It works for unit testing ios
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.all {
            linkerOpts("-lsqlite3")
        }
    }
    sourceSets.all {
        languageSettings.optIn("androidx.compose.animation.ExperimentalSharedTransitionApi")
    }
    applyDefaultHierarchyTemplate()

    androidTarget()
    jvm()
    iosArm64()
    iosSimulatorArm64()
    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../iosApp/Podfile")

        framework {
            baseName = "shared"
            isStatic = true

            export(libs.decompose)
            export(libs.essenty.lifecycle)
            export(libs.essenty.keeper)
            export(libs.essenty.backhandler)
        }
        xcodeConfigurationToNativeBuildType["debug"] = NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["release"] = NativeBuildType.RELEASE
        xcodeConfigurationToNativeBuildType["beta"] = NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["betarelease"] = NativeBuildType.RELEASE
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }


    sourceSets {
        commonMain.dependencies {

            implementation(projects.core.domain)
            implementation(projects.core.storage)
            implementation(projects.core.net)
            implementation(projects.core.ui)
            implementation(projects.core.navigation)
            implementation(projects.core.data)

            implementation(projects.pages.pageWorkspace)
            implementation(projects.pages.pageSelectWorkspace)

            implementation(libs.shared.logger.kermit)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)


            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)
            implementation(libs.videoplayer)

            implementation(libs.coroutines)
            implementation(libs.koin)
            implementation(libs.decompose)
            implementation(libs.mvi)
            implementation(libs.mvi.coroutines)
            implementation(libs.mvi.kotlin)


            implementation(libs.coil)
            implementation(libs.coil.svg)
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
            implementation(libs.colorpicker.compose)

            api(libs.essenty.lifecycle)
            api(libs.essenty.keeper)
            api(libs.essenty.backhandler)

            implementation(libs.decompose.compose)
            implementation(libs.decompose.compose.experimental)
            implementation(libs.constraintlayout.compose.multiplatform)

            implementation(libs.dnd)
            implementation(libs.webview)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
            implementation(compose.uiTest)
            implementation(libs.kotest.property)
        }
        jvmMain.dependencies {


            implementation(compose.desktop.common)
            implementation(compose.desktop.currentOs)
            implementation(libs.coil.network.jvm)
            implementation(libs.shared.core.coroutines.swing)
            implementation(libs.reaktive.reaktive)
            implementation(libs.reaktive.coroutinesInterop)

            implementation(libs.serialization)

            implementation(libs.jewel.decorated.window)
            implementation(libs.jewel.int.ui.standalone)
        }
        androidMain.dependencies {

        }
        iosMain.dependencies {
//            implementation(libs.compose.remote.layout.iosarm64)
        }
        androidUnitTest.dependencies {
            implementation(libs.robolectric)
            implementation(libs.android.videoplayer.contextprovider)
        }
        wasmJsMain.dependencies {

        }
    }
}

dependencies {
    commonMainApi(libs.decompose)
    commonMainApi(libs.essenty.keeper)
    commonMainApi(libs.essenty.lifecycle)
    debugApi(compose.uiTooling)
}
android {
    namespace = "com.moly3.cedarjam"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    buildFeatures.compose = true
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

compose {
    resources {
        publicResClass = false
        generateResClass = auto
    }
    desktop {
        application {
            buildTypes.release.proguard {
                isEnabled = false
                configurationFiles.from("compose-desktop.pro")
            }

            mainClass = "MainKt"

            jvmArgs("--add-opens=java.desktop/com.apple.eawt.event=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
            jvmArgs(
                "--add-opens",
                "java.desktop/java.awt.peer=ALL-UNNAMED"
            ) // recommended but not necessary

            if (System.getProperty("os.name").contains("Mac")) {
                jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
                jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
            }

            args("-XDignore.symbol.file --add-exports java.desktop/com.apple.eawt.event=ALL-UNNAMED")

            nativeDistributions {
                //appName = "CedarJam"
                modules(
                    "java.base",
                    "java.desktop",
                    "java.logging",
                    "java.sql",
                    "jdk.unsupported"
                )
                targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                packageName = "CedarJam"
                packageVersion = "1.0.0"

                macOS {
                    dockName = "CedarJam"
                    iconFile.set(project.file("../docs/media/AppIcon.icns"))
                }
            }
        }
    }
}