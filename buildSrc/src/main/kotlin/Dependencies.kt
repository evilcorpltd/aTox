import org.gradle.api.JavaVersion

const val kotlinVersion = "1.3.61"

object Java {
    val version = JavaVersion.VERSION_1_8
}

private object Version {
    const val coroutines = "1.3.2-1.3.60"
    const val dagger = "2.25.2"
    const val lifecycle = "2.2.0-rc02"
    const val room = "2.2.2"
    const val navigation = "2.2.0-rc02"
}

object BuildPlugin {
    private object Version {
        const val gradle = "3.5.2"
    }

    const val androidApplication = "com.android.application"
    const val androidLibrary = "com.android.library"
    const val kotlinAndroid = "kotlin-android"
    const val kotlinAndroidExtensions = "kotlin-android-extensions"
    const val kotlinKapt = "kotlin-kapt"

    const val gradle = "com.android.tools.build:gradle:${Version.gradle}"
    const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"

    const val download = "de.undercouch.download"
    const val downloadVersion = "4.0.2"
}

object AndroidSdk {
    const val minVersion = 21
    const val targetVersion = 29
}

object Libraries {
    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"

    const val javaxInject = "javax.inject:javax.inject:1"

    const val ktxCoroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.coroutines}"
    const val ktxCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Version.coroutines}"

    const val appcompat = "androidx.appcompat:appcompat:1.1.0"
    const val constraintlayout = "androidx.constraintlayout:constraintlayout:1.1.3"

    const val navigationFragmentKtx = "androidx.navigation:navigation-fragment-ktx:${Version.navigation}"
    const val navigationUiKtx = "androidx.navigation:navigation-ui-ktx:${Version.navigation}"

    const val lifecycleExtensions = "androidx.lifecycle:lifecycle-extensions:${Version.lifecycle}"

    const val roomRuntime = "androidx.room:room-runtime:${Version.room}"
    const val roomCompiler = "androidx.room:room-compiler:${Version.room}"

    const val material = "com.google.android.material:material:1.1.0-beta02"

    const val dagger = "com.google.dagger:dagger:${Version.dagger}"
    const val daggerAndroid = "com.google.dagger:dagger-android:${Version.dagger}"
    const val daggerAndroidSupport = "com.google.dagger:dagger-android-support:${Version.dagger}"
    const val daggerAndroidProcessor = "com.google.dagger:dagger-android-processor:${Version.dagger}"
    const val daggerCompiler = "com.google.dagger:dagger-compiler:${Version.dagger}"

    const val scalaLibrary = "org.scala-lang:scala-library:2.11.12"
    const val scalaLogging = "com.typesafe.scala-logging:scala-logging_2.11:3.9.2"
    const val scalapbRuntime = "com.trueaccord.scalapb:scalapb-runtime_2.11:0.6.7"
    const val scodecCore = "org.scodec:scodec-core_2.11:1.11.4"

    const val leakcanaryAndroid = "com.squareup.leakcanary:leakcanary-android:2.0-beta-5"

    const val junit = "junit:junit:4.12"
    const val runner = "androidx.test:runner:1.2.0"
    const val espressoCore = "androidx.test.espresso:espresso-core:3.2.0"
    const val androidJUnit = "androidx.test.ext:junit:1.1.1"
}

