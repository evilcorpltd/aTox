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
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = mapOf("room.incremental" to "true")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    compileOptions {
        sourceCompatibility = Java.version
        targetCompatibility = Java.version
    }
    kotlinOptions {
        require(this is KotlinJvmOptions)
        jvmTarget = Java.version.toString()
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
