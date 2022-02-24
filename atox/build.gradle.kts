plugins {
    id(BuildPlugin.androidApplication)

    id(BuildPlugin.kotlinAndroid)
    id(BuildPlugin.kotlinKapt)
    id("com.google.devtools.ksp") version "1.5.31-1.0.1" // Depends on your kotlin version
}

apply<KtlintPlugin>()

android {
    compileSdk = AndroidSdk.targetVersion
    defaultConfig {
        applicationId = "ltd.evilcorp.atox"
        minSdk = AndroidSdk.minVersion
        targetSdk = AndroidSdk.targetVersion
        versionCode = 13
        versionName = "0.7.3"
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
    signingConfigs {
        getByName("debug") {
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storeFile = file("debug.keystore")
            storePassword = "android"
        }
    }
    buildFeatures {
        viewBinding = true
    }
    lint {
        disable += setOf("GoogleAppIndexingWarning", "MissingTranslation")
    }
    packagingOptions {
        // Work around scala-compiler and scala-library (via tox4j) trying to place files in the
        // same place.
        resources.excludes.add("rootdoc.txt")
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))

    implementation(AndroidX.Core.ktx)
    implementation("androidx.core:core-splashscreen:1.0.0-beta01")
    implementation(AndroidX.activity)
    implementation(AndroidX.appcompat)
    implementation(AndroidX.constraintlayout)
    implementation(AndroidX.fragment)

    implementation(Google.Android.material)
    implementation(Google.Guava.workaround)

    implementation(KotlinX.Coroutines.core)
    implementation(KotlinX.Coroutines.android)

    implementation(AndroidX.Navigation.fragment)
    implementation(AndroidX.Navigation.ui)

    implementation(AndroidX.preference)

    implementation(AndroidX.Lifecycle.livedataKtx)
    implementation(AndroidX.Lifecycle.runtimeKtx)
    implementation(AndroidX.Lifecycle.service)
    implementation(AndroidX.Lifecycle.viewmodelKtx)

    val compose_version = "1.1.0"

    implementation("androidx.compose.ui:ui:$compose_version")
    implementation("androidx.compose.material:material:$compose_version")
    implementation("androidx.compose.ui:ui-tooling-preview:$compose_version")
    implementation("io.github.raamcosta.compose-destinations:core:1.3.1-beta")
    ksp("io.github.raamcosta.compose-destinations:ksp:1.3.1-beta")
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation(Google.dagger)
    kapt(Google.Dagger.compiler)

    implementation(AndroidX.multidex)

    implementation(Nayuki.qrcodegen)

    implementation(Square.picasso)

    debugImplementation(Square.leakcanary)

    testImplementation(Test.junit)

    androidTestImplementation(AndroidX.Test.rules)
    androidTestImplementation(AndroidX.Test.runner)
    androidTestImplementation(AndroidX.Test.Espresso.core)
    androidTestImplementation(AndroidX.Test.Espresso.contrib)
    androidTestImplementation(AndroidX.Test.Ext.junit)
    androidTestImplementation(Test.mockk)
    kaptAndroidTest(Google.Dagger.compiler)
}
