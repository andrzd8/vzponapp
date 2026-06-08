import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localProps = Properties()
file("../local.properties").inputStream().use { localProps.load(it) }

android {
    namespace = "si.uni_lj.fe.tnuv.vzponapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "si.uni_lj.fe.tnuv.vzponapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GEMINI_API_KEY", "\"${localProps["gemini_api_key"]}\"")
        buildConfigField("String", "WEATHER_API_KEY", "\"${localProps["weather_api_key"]}\"")
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

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.volley)
    implementation(libs.okhttp)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("org.osmdroid:osmdroid-android:6.1.20")
    implementation("androidx.credentials:credentials:1.6.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.6.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.2.0")
}