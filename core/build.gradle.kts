import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

plugins {
    id(BuildPlugin.androidLibrary)

    id(BuildPlugin.kotlinAndroid)
    id(BuildPlugin.kotlinKapt)
}

android {
    compileSdkVersion(AndroidSdk.targetVersion)
    defaultConfig {
        minSdkVersion(AndroidSdk.minVersion)
        targetSdkVersion(AndroidSdk.targetVersion)
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = Java.version
        targetCompatibility = Java.version
    }
    kotlinOptions {
        with(this as KotlinJvmOptions) {
            jvmTarget = Java.version.toString()
        }
    }
    lintOptions {
        isAbortOnError = true
        isWarningsAsErrors = true
    }
}

dependencies {
    // Stdlib
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.javaxInject)

    // Lifecycle
    api(Libraries.lifecycleExtensions)

    // Room
    api(Libraries.roomRuntime)
    kapt(Libraries.roomCompiler)

    // Dagger
    implementation(Libraries.dagger)
    kapt(Libraries.daggerCompiler)
}
