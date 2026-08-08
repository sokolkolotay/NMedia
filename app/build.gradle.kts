import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "ru.netology.nmedia"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.netology.nmedia"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val properties = Properties()
        listOf("local.properties", "mapkit.properties").forEach { fileName ->
            val propertyFile = rootProject.file(fileName)
            if (propertyFile.exists()) {
                properties.load(propertyFile.inputStream())
            }
        }

        val mapKitApiKey = properties.getProperty("MAPKIT_API_KEY", "")
        buildConfigField("String", "MAPKIT_API_KEY", "\"$mapKitApiKey\"")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.recyclerview)
    implementation(libs.material)
    implementation(libs.yandex.mapkit)
}
