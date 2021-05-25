plugins {
    id(BuildPlugin.androidApplication)

    id(BuildPlugin.kotlinAndroid)
    id(BuildPlugin.kotlinKapt)
}

apply<KtlintPlugin>()

android {
    compileSdkVersion(AndroidSdk.targetVersion)
    defaultConfig {
        applicationId = "ltd.evilcorp.atox"
        minSdkVersion(AndroidSdk.minVersion)
        targetSdkVersion(AndroidSdk.targetVersion)
        versionCode = 9
        versionName = "0.6.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles("proguard-tox4j.pro", getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    buildFeatures {
        viewBinding = true
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
        disable("GoogleAppIndexingWarning", "MissingTranslation", "InvalidPackage")
    }
    sourceSets["main"].java.srcDir("src/main/kotlin")
    sourceSets["androidTest"].java.srcDir("src/androidTest/kotlin")
    packagingOptions {
        // Work around scala-compiler and scala-library (via tox4j) trying to place files in the
        // same place.
        exclude("rootdoc.txt")
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.google.android.material)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.androidx.preference)

    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.google.dagger.core)
    kapt(libs.google.dagger.compiler)

    implementation(libs.androidx.multidex)

    implementation(libs.square.picasso)

    debugImplementation(libs.square.leakcanary)

    testImplementation(libs.test.junit.core)

    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.espresso.core)
    androidTestImplementation(libs.test.espresso.contrib)
    androidTestImplementation(libs.test.junit.ext)
    androidTestImplementation(libs.test.mockk)
    kaptAndroidTest(libs.google.dagger.compiler)
}
