import org.gradle.api.JavaVersion

const val kotlinVersion = "1.3.72"

object Java {
    val version = JavaVersion.VERSION_1_8
}

private object Version {
    const val coroutines = "1.3.6"
    const val dagger = "2.27"
    const val leakCanary = "2.3"
    const val lifecycle = "2.2.0"
    const val room = "2.2.5"
    const val navigation = "2.2.0"
    const val mockk = "1.10.0"
    const val toxcore = "0.2.12"
    const val sadToxcore = "0.2.2" // The x86 build has apparently been broken for a while.
    const val tox4j = "0.2.3"
}

object BuildPlugin {
    private object Version {
        const val gradle = "4.0.0"
    }

    const val androidApplication = "com.android.application"
    const val androidLibrary = "com.android.library"
    const val kotlinAndroid = "kotlin-android"
    const val kotlinAndroidExtensions = "kotlin-android-extensions"
    const val kotlinKapt = "kotlin-kapt"

    const val gradle = "com.android.tools.build:gradle:${Version.gradle}"
    const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"

    const val ideaExt = "org.jetbrains.gradle.plugin.idea-ext"
    const val ideaExtVersion = "0.7"

    const val versions = "com.github.ben-manes.versions"
    const val versionsVersion = "0.28.0"
    const val gradleVersionsPlugin = "com.github.ben-manes:gradle-versions-plugin:$versionsVersion"
}

object AndroidSdk {
    const val minVersion = 19
    const val targetVersion = 29
}

object Libraries {
    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"

    const val javaxInject = "javax.inject:javax.inject:1"

    const val ktxCoroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.coroutines}"
    const val ktxCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Version.coroutines}"

    const val androidxCoreKtx = "androidx.core:core-ktx:1.2.0"

    const val appcompat = "androidx.appcompat:appcompat:1.1.0"
    const val constraintlayout = "androidx.constraintlayout:constraintlayout:1.1.3"

    const val navigationFragmentKtx = "androidx.navigation:navigation-fragment-ktx:${Version.navigation}"
    const val navigationUiKtx = "androidx.navigation:navigation-ui-ktx:${Version.navigation}"

    const val lifecycleExtensions = "androidx.lifecycle:lifecycle-extensions:${Version.lifecycle}"

    const val multidex = "androidx.multidex:multidex:2.0.1"

    const val roomRuntime = "androidx.room:room-runtime:${Version.room}"
    const val roomCompiler = "androidx.room:room-compiler:${Version.room}"

    const val preference = "androidx.preference:preference:1.1.1"

    const val material = "com.google.android.material:material:1.1.0"

    const val dagger = "com.google.dagger:dagger:${Version.dagger}"
    const val daggerAndroid = "com.google.dagger:dagger-android:${Version.dagger}"
    const val daggerAndroidSupport = "com.google.dagger:dagger-android-support:${Version.dagger}"
    const val daggerAndroidProcessor = "com.google.dagger:dagger-android-processor:${Version.dagger}"
    const val daggerCompiler = "com.google.dagger:dagger-compiler:${Version.dagger}"

    // 3.6.0 is the last version before API 24 was required.
    const val zxingAndroidEmbedded = "com.journeyapps:zxing-android-embedded:3.6.0"

    const val toxcore_x86_64 = "org.toktok:tox4j-c_x86_64-linux-android:${Version.toxcore}"
    const val toxcore_i686 = "org.toktok:tox4j-c_i686-linux-android:${Version.sadToxcore}"
    const val toxcore_arm = "org.toktok:tox4j-c_arm-linux-androideabi:${Version.toxcore}"
    const val toxcore_aarch64 = "org.toktok:tox4j-c_aarch64-linux-android:${Version.toxcore}"

    const val tox4jApi = "org.toktok:tox4j-api_2.11:${Version.tox4j}"
    const val tox4jC = "org.toktok:tox4j-c_2.11:${Version.tox4j}"

    const val leakcanaryAndroid = "com.squareup.leakcanary:leakcanary-android:${Version.leakCanary}"

    const val junit = "junit:junit:4.13"
    const val runner = "androidx.test:runner:1.2.0"
    const val espressoCore = "androidx.test.espresso:espresso-core:3.2.0"
    const val androidJUnit = "androidx.test.ext:junit:1.1.1"
    const val roomTesting = "androidx.room:room-testing:${Version.room}"

    const val mockk = "io.mockk:mockk-android:${Version.mockk}"
}
