@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKsp)
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "ltd.evilcorp.core"
    compileSdk = libs.versions.sdk.target.get().toInt()
    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    sourceSets["androidTest"].assets.srcDir("$projectDir/schemas")
    // TODO(robinlinden): Not needed w/ AGP 8.1.0-alpha09.
    //  https://issuetracker.google.com/issues/260059413?pli=1
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.javax.inject)
    api(libs.kotlinx.coroutines.core)
    api(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(kotlin("test"))

    androidTestImplementation(kotlin("test"))
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.junit.ext)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test) {
        // Conflicts with a lot of things due to having embedded "byte buddy" instead of depending on it.
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-debug")
    }
}
