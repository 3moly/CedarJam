rootProject.name = "CedarJam"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://jogamp.org/deployment/maven")
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jogamp.org/deployment/maven")
        maven { url = uri("https://releases.aspose.com/java/repo/") }
    }
}

include(
    ":androidApp",
    ":shared",

    ":core:domain",
    ":core:storage",
    ":core:ui",
    ":core:navigation",
    ":core:net",
    ":core:data",

    ":features:feature-graph",
    ":features:feature-canvas",
    ":features:feature-browser",
    ":features:feature-file",
    ":features:feature-file-view",
    ":features:feature-settings",

    ":pages:page-home",
    ":pages:page-file",
    ":pages:page-collection",
    ":pages:page-collection-row",
    ":pages:page-graph",
    ":pages:page-tags",
    ":pages:page-tag",
    ":pages:page-tabs",
    ":pages:page-tab",
    ":pages:page-select-workspace",
    ":pages:page-workspace",
)
include(":desktopApp")
include(":benchmark")
include(":core:coordinator")
