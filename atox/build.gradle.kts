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

    implementation(AndroidX.Core.ktx)
    implementation(AndroidX.appcompat)
    implementation(AndroidX.constraintlayout)

    implementation(Google.Android.material)

    implementation(KotlinX.Coroutines.core)
    implementation(KotlinX.Coroutines.android)

    implementation(AndroidX.Navigation.fragmentKtx)
    implementation(AndroidX.Navigation.uiKtx)

    implementation(AndroidX.preference)

    implementation(AndroidX.Lifecycle.livedataKtx)
    implementation(AndroidX.Lifecycle.service)
    implementation(AndroidX.Lifecycle.viewmodelKtx)

    implementation(Google.dagger)
    kapt(Google.Dagger.compiler)

    implementation(AndroidX.multidex)

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
