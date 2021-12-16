plugins {
    id(BuildPlugin.androidApplication)

    id(BuildPlugin.kotlinAndroid)
    id(BuildPlugin.kotlinKapt)
}

apply<KtlintPlugin>()

android {
    compileSdk = AndroidSdk.targetVersion
    defaultConfig {
        applicationId = "ltd.evilcorp.atox"
        minSdk = AndroidSdk.minVersion
        targetSdk = AndroidSdk.targetVersion
        versionCode = 9
        versionName = "0.6.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            proguardFiles("proguard-tox4j.pro", getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    buildFeatures {
        viewBinding = true
    }
    lint {
        disable("GoogleAppIndexingWarning", "MissingTranslation", "InvalidPackage")
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
    implementation(AndroidX.Lifecycle.service)
    implementation(AndroidX.Lifecycle.viewmodelKtx)

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
