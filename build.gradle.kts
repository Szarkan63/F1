import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("plugin.serialization") version "1.9.0"
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.fryzjer"
    compileSdk = 35
    buildFeatures {
        buildConfig = true
    }
    // Odczyt `local.properties`
    val localProperties = Properties()
    val propertiesFile = File(rootDir, "local.properties")
    if (propertiesFile.exists()) {
        localProperties.load(propertiesFile.inputStream())
    }
    val service_key: String = localProperties.getProperty("service_key") ?: ""
    val key: String = localProperties.getProperty("supabaseKey") ?: ""
    val url: String = localProperties.getProperty("supabaseUrl") ?: ""


    defaultConfig {
        applicationId = "com.example.fryzjer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("String", "supabaseKey", "\"${key}\"")
        buildConfigField("String", "supabaseUrl", "\"${url}\"")
        buildConfigField("String", "service_key", "\"${service_key}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.play.services.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.material3:material3-window-size-class:1.3.1")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.4.0-alpha05")
    implementation("androidx.compose.material:material-icons-extended:1.7.6") // For icons
    implementation("androidx.compose.material3:material3:1.4.0-alpha02")
    val nav_version = "2.8.5"

    //noinspection GradleDependency
    implementation("androidx.navigation:navigation-compose:$nav_version")
    //Supabase
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.1"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.ktor:ktor-client-android:3.0.1")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation(libs.androidx.activity.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")


    //
}