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
    alias(libs.plugins.stability.analyzer)
    kotlin("native.cocoapods")
    alias(libs.plugins.metro)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.JETBRAINS)
    }
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
//        withDeviceTest {
//            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//            execution = "HOST"
//        }
        withHostTest {
            isIncludeAndroidResources = true
        }
//        testOptions {
//            unitTests {
//                isIncludeAndroidResources = true
//            }
//        }
        namespace = "com.moly3.cedarjam"
        compileSdk = 36
    }
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
            implementation(projects.core.coordinator)

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

            implementation(libs.ktor.cio)
            implementation(libs.ktor)
            implementation(libs.ktor.serialization)
            implementation(libs.ktor.contentnegotiation)
            implementation(libs.ktor.logging)
            implementation(libs.ktor.json)

//            implementation(libs.desktop)
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
            implementation(libs.robolectric)
        }
        iosMain.dependencies {
//            implementation(libs.compose.remote.layout.iosarm64)
        }

        androidUnitTest.dependencies {
            implementation(libs.robolectric)
            implementation(libs.android.videoplayer.contextprovider)
        }
        androidInstrumentedTest.dependencies {
            implementation(libs.robolectric)
            implementation(libs.android.videoplayer.contextprovider)
        }
        linuxX64Main.dependencies {
            implementation("org.jetbrains.compose.desktop:desktop-jvm-linux-x64:1.10.1")
        }
        macosX64Main.dependencies {
            implementation("org.jetbrains.compose.desktop:desktop-jvm-macos-x64:1.10.1")
        }
        macosArm64Main.dependencies {
            implementation("org.jetbrains.compose.desktop:desktop-jvm-macos-arm64:1.10.1")
        }
        mingwX64Main.dependencies {
            implementation("org.jetbrains.compose.desktop:desktop-jvm-windows-x64:1.10.1")
        }
    }
}

dependencies {
    commonMainApi(libs.decompose)
    commonMainApi(libs.essenty.keeper)
    commonMainApi(libs.essenty.lifecycle)

    // Use the configurations created by the Conveyor plugin to tell Gradle/Conveyor where to find the artifacts for each platform.
//    linuxAmd64(compose.desktop.linux_x64)
//    macAmd64(compose.desktop.macos_x64)
//    macAarch64(compose.desktop.macos_arm64)
//    windowsAmd64(compose.desktop.windows_x64)
}

//configurations.all {
//    attributes {
//        attribute(Attribute.of("ui", String::class.java), "awt")
//    }
//}


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


// Use environment variables on CI, fallback to local.properties otherwise

fun isCI(): Boolean {
    return providers.environmentVariable("CI").isPresent
}

val syncServerUrlProvider = if (isCI()) {
    providers.environmentVariable("SYNC_SERVER_URL")
} else {
    providers.provider { mySyncServerUrl }
}

val syncServerTokenProvider = if (isCI()) {
    providers.environmentVariable("SYNC_SERVER_TOKEN")
} else {
    providers.provider { mySyncServerToken }
}

val isReleaseProvider = if (isCI()) {
    providers.environmentVariable("IS_RELEASE").map { it.toBoolean() }
} else {
    providers.provider { myIsRelease.toBoolean() }
}

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

        println("CI = ${System.getenv("CI")}")

        println("SYNC_SERVER_URL = ${syncServerUrl.get()}")
        println("SYNC_SERVER_TOKEN = ${syncServerToken.get()}")
        println("IS_RELEASE = ${syncJustToken.get()}")

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
    syncServerUrl.set(syncServerUrlProvider)
    syncServerToken.set(syncServerTokenProvider)
    syncJustToken.set(providers.provider { isReleaseProvider.get().toString() })
    outputDir.set(layout.buildDirectory.dir("generated/source/buildConfig/commonMain/kotlin"))
}

// Add generated source to the commonMain source set
kotlin.sourceSets.named("commonMain") {
    kotlin.srcDir(generateBuildConfig.map { it.outputDir })
}