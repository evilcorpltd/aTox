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
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
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
        freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
    }
    lintOptions {
        // TODO(robinlinden): Delete/update invalid packages
        disable("InvalidPackage", "GoogleAppIndexingWarning", "MissingTranslation")
    }
    sourceSets["main"].java.srcDir("src/main/kotlin")
    sourceSets["androidTest"].java.srcDir("src/androidTest/kotlin")
}

dependencies {
    implementation(Libraries.kotlinStdLib)

    implementation(project(":core"))
    implementation(project(":domain"))

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

    implementation(Libraries.multidex)

    implementation(Libraries.zxingAndroidEmbedded)

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
