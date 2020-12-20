import org.gradle.api.JavaVersion

const val kotlinVersion = "1.4.21"

object Java {
    val version = JavaVersion.VERSION_1_8
}

object BuildPlugin {
    private object Version {
        const val gradle = "4.1.1"
    }

    const val androidApplication = "com.android.application"
    const val androidLibrary = "com.android.library"
    const val kotlinAndroid = "kotlin-android"
    const val kotlinKapt = "kotlin-kapt"

    const val gradle = "com.android.tools.build:gradle:${Version.gradle}"
    const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"

    const val ideaExt = "org.jetbrains.gradle.plugin.idea-ext"
    const val ideaExtVersion = "0.10"

    const val versions = "com.github.ben-manes.versions"
    const val versionsVersion = "0.36.0"
    const val gradleVersionsPlugin = "com.github.ben-manes:gradle-versions-plugin:$versionsVersion"
}

object AndroidSdk {
    const val minVersion = 19
    const val targetVersion = 29
}

object JavaX {
    const val inject = "javax.inject:javax.inject:1"
}

object KotlinX {
    object Coroutines {
        private const val version = "1.4.2"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
    }
}

object AndroidX {
    object Core {
        const val ktx = "androidx.core:core-ktx:1.3.2"
    }

    const val appcompat = "androidx.appcompat:appcompat:1.2.0"
    const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.0.1"

    object Navigation {
        private const val version = "2.3.2"
        const val fragmentKtx = "androidx.navigation:navigation-fragment-ktx:$version"
        const val uiKtx = "androidx.navigation:navigation-ui-ktx:$version"
    }

    object Lifecycle {
        private const val version = "2.2.0"
        const val extensions = "androidx.lifecycle:lifecycle-extensions:$version"
        const val livedataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
        const val viewmodelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
    }

    const val multidex = "androidx.multidex:multidex:2.0.1"

    object Room {
        private const val version = "2.2.5"
        const val runtime = "androidx.room:room-runtime:$version"
        const val ktx = "androidx.room:room-ktx:$version"
        const val compiler = "androidx.room:room-compiler:$version"
        const val testing = "androidx.room:room-testing:$version"
    }

    const val preference = "androidx.preference:preference:1.1.1"

    object Test {
        private const val version = "1.3.0"
        const val runner = "androidx.test:runner:$version"
        const val rules = "androidx.test:rules:$version"
        object Espresso {
            private const val version = "3.3.0"
            const val core = "androidx.test.espresso:espresso-core:$version"
            const val contrib = "androidx.test.espresso:espresso-contrib:$version"
        }
        object Ext {
            const val junit = "androidx.test.ext:junit:1.1.2"
        }
    }
}

object Google {
    object Android {
        const val material = "com.google.android.material:material:1.2.1"
    }

    const val dagger = "com.google.dagger:dagger:${Dagger.version}"
    object Dagger {
        internal const val version = "2.30.1"
        const val compiler = "com.google.dagger:dagger-compiler:$version"
    }
}

object JourneyApps {
    // 3.6.0 is the last version before API 24 was required.
    const val zxing = "com.journeyapps:zxing-android-embedded:3.6.0"
}

object Square {
    const val picasso = "com.squareup.picasso:picasso:2.8"
    const val leakcanary = "com.squareup.leakcanary:leakcanary-android:2.5"
}

object Tox4j {
    private const val version = "0.2.3"
    const val api = "org.toktok:tox4j-api_2.11:$version"
    const val c = "org.toktok:tox4j-c_2.11:$version"
    object Android {
        private const val version = "0.2.12"
        private const val oldVersion = "0.2.2" // The x86 build has been broken for a while.
        const val x86_64 = "org.toktok:tox4j-c_x86_64-linux-android:$version"
        const val arm = "org.toktok:tox4j-c_arm-linux-androideabi:$version"
        const val aarch64 = "org.toktok:tox4j-c_aarch64-linux-android:$version"
        const val i686 = "org.toktok:tox4j-c_i686-linux-android:$oldVersion"
    }
}

object Test {
    const val junit = "junit:junit:4.13.1"
    const val mockk = "io.mockk:mockk-android:1.10.0"
}
