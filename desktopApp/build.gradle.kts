import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
    id("dev.hydraulic.conveyor") version "1.13"
}

version = "1.0"

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.JETBRAINS)
    }
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.shared)

                implementation(libs.kotlin.stdlib)
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
                implementation(libs.compose.components.resources)
//            implementation(libs.compose.ui.tooling.preview)


                implementation(libs.filekit.core)
                implementation(libs.filekit.dialogs)
                implementation(libs.videoplayer)

                implementation(libs.coroutines)

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
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.coil.network.jvm)
            implementation(libs.shared.core.coroutines.swing)
            implementation(libs.reaktive.reaktive)
            implementation(libs.reaktive.coroutinesInterop)
            implementation(libs.jewel.decorated.window)
            implementation(libs.jewel.int.ui.standalone)
            implementation(libs.serialization)
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

            //javaHome = "/Users/new07/Library/Java/JavaVirtualMachines/jbr-21.0.10/Contents/Home"
            buildTypes.release.proguard {
                isEnabled = false
                optimize = false
                obfuscate = false
                version.set("7.8.2")
                configurationFiles.from("compose-desktop.pro")
            }

            mainClass = "com.moly3.app.MainKt"

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
                packageVersion = "1.0.1"
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


                linux {
                    packageName = "cedarjam"
                    iconFile.set(project.file("../docs/media/cone.png"))
                }
                macOS {
                    minimumSystemVersion = "14.0"
                    dockName = "CedarJam"

                    iconFile.set(project.file("../docs/media/AppIcon.icns"))

                    bundleID = "com.moly3.cedarjam"
                    infoPlist {
                        extraKeysRawXml = """
                            <key>LSMinimumSystemVersion</key>
                            <string>14.0</string>
                        """.trimIndent()
                    }
                }
                windows {
                    //todo iconFile.set(project.file("../docs/media/AppIcon.ico"))
                }

//                appResourcesRootDir = layout.projectDirectory.dir("src/desktopMain/assets")
//                jvmArgs += "-splash:${'$'}APPDIR/resources/splash.png"
            }
        }
    }
}

dependencies {
    // Use the configurations created by the Conveyor plugin to tell Gradle/Conveyor where to find the artifacts for each platform.
    linuxAmd64(compose.desktop.linux_x64)
    macAmd64(compose.desktop.macos_x64)
    macAarch64(compose.desktop.macos_arm64)
    windowsAmd64(compose.desktop.windows_x64)
}