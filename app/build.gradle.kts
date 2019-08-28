import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(BuildPlugin.androidApplication)

    id(BuildPlugin.kotlinAndroid)
    id(BuildPlugin.kotlinAndroidExtensions)
    id(BuildPlugin.kotlinKapt)
}

android {
    compileSdkVersion(AndroidSdk.targetVersion)
    defaultConfig {
        applicationId = "ltd.evilcorp.atox"
        minSdkVersion(AndroidSdk.minVersion)
        targetSdkVersion(AndroidSdk.targetVersion)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
            freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
        }
    }
    lintOptions {
        isAbortOnError = true
        isWarningsAsErrors = true
        // TODO(robinlinden): Delete/update invalid packages
        disable("InvalidPackage", "GoogleAppIndexingWarning")
    }
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })

    implementation(Libraries.kotlinStdLib)

    implementation(project(":core"))

    implementation(Libraries.appcompat)
    implementation(Libraries.constraintlayout)
    implementation(Libraries.material)
    implementation(Libraries.ktxCoroutinesCore)
    implementation(Libraries.ktxCoroutinesAndroid)

    implementation(Libraries.navigationFragmentKtx)
    implementation(Libraries.navigationUiKtx)

    implementation(Libraries.dagger)
    implementation(Libraries.daggerAndroid)
    implementation(Libraries.daggerAndroidSupport)
    kapt(Libraries.daggerCompiler)
    kapt(Libraries.daggerAndroidProcessor)

    // For tox4j
    // TODO(robinlinden): Fix tox4j build so we can update the scala dependencies
    // noinspection GradleDependency
    implementation(Libraries.scalaLibrary)
    implementation(Libraries.scalaLogging)
    implementation(Libraries.scalapbRuntime)
    implementation(Libraries.scodecCore)

    debugImplementation(Libraries.leakcanaryAndroid)

    testImplementation(Libraries.junit)
    androidTestImplementation(Libraries.runner)
    androidTestImplementation(Libraries.espressoCore)
    androidTestImplementation(Libraries.androidJUnit)
}

tasks.withType<KotlinCompile> {
    // newSingleThreadContext
    kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.coroutines.ObsoleteCoroutinesApi")
}
