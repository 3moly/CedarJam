import com.android.build.api.variant.VariantOutputConfiguration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}
val versionCode = 1
val versionName = "1.0"

android {
    namespace = "com.moly3.cedarjam.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()


    defaultConfig {
        applicationId = "com.moly3.cedarjam.android"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.compileSdk.get().toInt()
        this.versionCode = versionCode
        this.versionName = versionName
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

base {
    val dateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date())
    val fileName = "${rootProject.name}_v${versionName}_${versionCode}_${formattedDate}"

    archivesName = fileName
//        "${android.defaultConfig.applicationId}-${android.defaultConfig.versionName}-${android.defaultConfig.versionCode}"
}


dependencies {
    implementation(projects.shared)
    implementation(projects.core.domain)

    implementation(libs.androidx.activity.compose)
    implementation(libs.filekit.core)
    implementation(libs.filekit.dialogs)
    implementation(libs.decompose)
    implementation(libs.decompose.compose)

    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
//    implementation(libs.androidx.profileinstaller)

//    project(":baselineprofile")
}
