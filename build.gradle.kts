import dev.iurysouza.modulegraph.ModuleType
import dev.iurysouza.modulegraph.Theme

plugins {
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.kotlinCocoapods).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.sqlDelight).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.serialization).apply(false)
    alias(libs.plugins.modulegraph.mermaid)
    alias(libs.plugins.android.kotlin.multiplatform.library).apply(false)
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.metro).apply(false)
}

buildscript {
    repositories {
        gradlePluginPortal()
        // Desktop target has to add this repo
        maven("https://jogamp.org/deployment/maven")
        google()
        mavenCentral()
        maven { url = uri("https://jogamp.org/deployment/maven") }
        maven("https://packages.jetbrains.team/maven/p/kpm/public/")
    }
}
moduleGraphConfig {
    heading.set("### Project's Structure")
    readmePath = "${rootDir}/docs/projectStructure.md"
    rootModulesRegex.set(":shared")

    theme.set(
        Theme.BASE(
            mapOf(
                "primaryTextColor" to "#fff",
                "primaryColor" to "#5a4f7c",
                "primaryBorderColor" to "#5a4f7c",
                "lineColor" to "#00a623",
                "tertiaryColor" to "#40375c",
                "fontSize" to "12px",
            ),
            focusColor = "#FA8140",
            moduleTypes = listOf(
                ModuleType.AndroidLibrary("#2C4162"),
            )
        ),
    )
}