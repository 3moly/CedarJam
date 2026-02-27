@file:OptIn(ExperimentalComposeLibrary::class)

import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
    alias(libs.plugins.composeHotReload)
    kotlin("native.cocoapods")
}


kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
    }
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
    android {
        namespace = "com.moly3.cedarjam"
        compileSdk = 36
//        defaultConfig {
//            minSdk = 26
//        }
//        buildFeatures.compose = true
//        compileOptions {
//            sourceCompatibility = JavaVersion.VERSION_17
//            targetCompatibility = JavaVersion.VERSION_17
//        }
//        defaultConfig {
//            minSdk = 26
//        }
//        buildFeatures.compose = true
//        compileOptions {
//            sourceCompatibility = JavaVersion.VERSION_17
//            targetCompatibility = JavaVersion.VERSION_17
//        }
//        testOptions {
//            unitTests {
//                isIncludeAndroidResources = true
//            }
//        }
    }
//    androidTarget()
//    androidTarget {
//
//    }
//    android{
//
//    }
//    androidLibrary {
////        withHostTest {
////            isIncludeAndroidResources = true
////        }
////        withJava()
////        withHostTest {
////            isIncludeAndroidResources = true
////        }
//
//        // Opt-in to enable and configure device-side (instrumented) tests
////        withDeviceTest {
////            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
////            execution = "HOST"
////        }
//
////        sourceSets["androidTest"].java.srcDirs("src/androidUnitTest/kotlin")
//
//        namespace = "com.moly3.cedarjam"
//        compileSdk = libs.versions.android.compileSdk.get().toInt()
//        minSdk = libs.versions.android.minSdk.get().toInt()
//        compilerOptions {
//            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
//        }
//        androidResources {
//            enable = true
//        }
//        withDeviceTest {
//            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//            execution = "HOST"
//        }
//        defaultConfig {
//            minSdk = 26
//        }
//        buildFeatures.compose = true
//        compileOptions {
//            sourceCompatibility = JavaVersion.VERSION_17
//            targetCompatibility = JavaVersion.VERSION_17
//        }
//        testOptions {
//            unitTests {
//                isIncludeAndroidResources = true
//            }
//        }
//    }
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

//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        browser {
//            commonWebpackConfig {
//                outputFileName = "composeApp.js"
//            }
//        }
//        binaries.executable()
//    }


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
            implementation(projects.pages.pageTab)
            implementation(projects.pages.pageTabs)

            implementation(libs.shared.logger.kermit)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.material)
            implementation(libs.compose.material3)
            implementation(libs.material.icons.extended)
            implementation(libs.compose.components.resources)
//            implementation(libs.compose.ui.tooling.preview)


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
            implementation(libs.ui.test)
            implementation(libs.kotest.property)
        }
        jvmMain.dependencies {


            implementation(libs.desktop)
            implementation(compose.desktop.currentOs)
            implementation(libs.coil.network.jvm)
            implementation(libs.shared.core.coroutines.swing)
            implementation(libs.reaktive.reaktive)
            implementation(libs.reaktive.coroutinesInterop)

            implementation(libs.jewel.decorated.window)
            implementation(libs.jewel.int.ui.standalone)

            implementation(libs.serialization)

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
    }
}

dependencies {
    commonMainApi(libs.decompose)
    commonMainApi(libs.essenty.keeper)
    commonMainApi(libs.essenty.lifecycle)
}


compose {
    resources {
        publicResClass = false
        generateResClass = auto
    }
    desktop {
        application {
            //javaHome = "/Users/new07/Library/Java/JavaVirtualMachines/jbr-21.0.10/Contents/Home"
            buildTypes.release.proguard {
                isEnabled = false
                optimize = false
                obfuscate = false
                version.set("7.8.2")
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
//                appResourcesRootDir.set(file("appResources"))
                //appName = "CedarJam"

                // https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Native_distributions_and_local_execution/README.md
                modules(
                    "java.compiler",
                    "java.instrument",
                    "java.management",
                    "java.rmi",
                    "jdk.unsupported",
                    "java.naming",
                    "java.base",
                    "java.desktop",
                    "java.logging",
                    "java.sql",
                )
                targetFormats(
                    TargetFormat.Dmg,
                    TargetFormat.Msi,
                    TargetFormat.Exe,
                    TargetFormat.Deb
                )
                packageName = "CedarJam"
                packageVersion = "1.0.0"

                macOS {
                    dockName = "CedarJam"
                    iconFile.set(project.file("../docs/media/AppIcon.icns"))
                }

//                appResourcesRootDir = layout.projectDirectory.dir("src/desktopMain/assets")
//                jvmArgs += "-splash:${'$'}APPDIR/resources/splash.png"
            }
        }
    }
}


//tasks.withType<JavaExec> {
//    systemProperty("compose.application.resources.dir", file("appResources").absolutePath)
//}

val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

val mySyncServerUrl = localProperties.getProperty("cedarjam_server.url") ?: ""
val mySyncServerToken = localProperties.getProperty("cedarjam_server.token") ?: ""
val myIsRelease = localProperties.getProperty("cedarjam.is_release") ?: ""

// Register cache-safe task
abstract class GenerateBuildConfigTask : DefaultTask() {
    @get:Input
    abstract val syncServerUrl: Property<String>

    @get:Input
    abstract val syncServerToken: Property<String>

    @get:Input
    abstract val syncJustToken: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val outputFile = outputDir.get().file("BuildConfig.kt").asFile

        val url = syncServerUrl.orNull
        val token = syncServerToken.orNull

        // CI safety check
        if (System.getenv("CI") == "true") {
            require(!url.isNullOrBlank()) { "SYNC_SERVER_URL is missing in CI" }
            require(!token.isNullOrBlank()) { "SYNC_SERVER_TOKEN is missing in CI" }
        }

        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            """
            package com.moly3.core_domain

            object BuildConfig {
                const val SyncServerUrl = "${syncServerUrl.get()}"
                const val SyncServerToken = "${syncServerToken.get()}"
                const val IsRelease = ${syncJustToken.get()}
            }
            """.trimIndent()
        )
    }
}

val generateBuildConfig by tasks.registering(GenerateBuildConfigTask::class) {
    syncServerUrl.set(mySyncServerUrl)
    syncServerToken.set(mySyncServerToken)
    syncJustToken.set(myIsRelease)
    outputDir.set(layout.buildDirectory.dir("generated/source/buildConfig/commonMain/kotlin"))
}

// Add generated source to the commonMain source set
kotlin.sourceSets.named("commonMain") {
    kotlin.srcDir(generateBuildConfig.map { it.outputDir })
}