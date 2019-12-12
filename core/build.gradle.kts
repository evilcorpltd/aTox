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
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = mapOf(
                    "room.incremental" to "true",
                    "room.schemaLocation" to "$projectDir/schemas"
                )
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
        jvmTarget = Java.version.toString()
    }
    lintOptions {
        isAbortOnError = true
        isWarningsAsErrors = true
    }
    sourceSets["androidTest"].assets.srcDir(files("$projectDir/schemas"))
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

    androidTestImplementation(Libraries.runner)
    androidTestImplementation(Libraries.androidJUnit)
    androidTestImplementation(Libraries.roomTesting)
}
