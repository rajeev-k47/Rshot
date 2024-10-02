import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "net.runner.rshot"
    compileSdk = 34

    buildFeatures{
        viewBinding= true
        buildConfig =true
    }
    defaultConfig {
        applicationId = "net.runner.rshot"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        val properties =  Properties()
        properties.load(project.rootProject.file("apikeys.properties").inputStream())
        buildConfigField("String","ONESIGNAL_API","\"${properties.getProperty("ONESIGNAL_API","")}\"")
        buildConfigField("String","GENERATIVE_API_KEY","\"${properties.getProperty("GENERATIVE_API_KEY","")}\"")
//        resValue()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val properties =  Properties()
            properties.load(project.rootProject.file("apikeys.properties").inputStream())
            buildConfigField("String","ONESIGNAL_API","\"${properties.getProperty("ONESIGNAL_API","")}\"")
            buildConfigField("String","GENERATIVE_API_KEY","\"${properties.getProperty("GENERATIVE_API_KEY","")}\"")
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation (libs.accompanist.systemuicontroller)
    implementation(libs.coil.compose)

    implementation ("com.onesignal:OneSignal:[5.0.0, 5.99.99]")
    implementation(libs.generativeai.v070)
    implementation("com.rmtheis:tess-two:9.1.0")
    implementation(libs.glide)
    implementation("com.github.bumptech.glide:okhttp3-integration:4.15.1")







}