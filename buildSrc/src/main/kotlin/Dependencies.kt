const val kotlinVersion = "1.6.10"

object BuildPlugin {
    private object Version {
        const val gradle = "7.1.0"
    }

    const val androidApplication = "com.android.application"
    const val androidLibrary = "com.android.library"
    const val kotlinAndroid = "kotlin-android"
    const val kotlinKapt = "kotlin-kapt"

    const val gradle = "com.android.tools.build:gradle:${Version.gradle}"
    const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"

    const val ideaExt = "org.jetbrains.gradle.plugin.idea-ext"
    const val ideaExtVersion = "1.1.1"

    const val versions = "com.github.ben-manes.versions"
    const val versionsVersion = "0.41.0"
    const val gradleVersionsPlugin = "com.github.ben-manes:gradle-versions-plugin:$versionsVersion"
}

object AndroidSdk {
    const val minVersion = 19
    const val targetVersion = 31
}

object JavaX {
    const val inject = "javax.inject:javax.inject:1"
}

object KotlinX {
    object Coroutines {
        private const val version = "1.6.0"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
    }
}

object AndroidX {
    object Core {
        const val ktx = "androidx.core:core-ktx:1.7.0"
    }

    const val activity = "androidx.activity:activity:1.4.0"
    const val appcompat = "androidx.appcompat:appcompat:1.4.1"
    const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.1.3"
    const val fragment = "androidx.fragment:fragment:1.4.0"

    object Navigation {
        private const val version = "2.4.0-rc01"
        const val fragment = "androidx.navigation:navigation-fragment:$version"
        const val ui = "androidx.navigation:navigation-ui:$version"
    }

    object Lifecycle {
        private const val version = "2.4.0"
        const val livedataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
        const val service = "androidx.lifecycle:lifecycle-service:$version"
        const val viewmodelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
    }

    const val multidex = "androidx.multidex:multidex:2.0.1"

    object Room {
        private const val version = "2.4.1"
        const val runtime = "androidx.room:room-runtime:$version"
        const val ktx = "androidx.room:room-ktx:$version"
        const val compiler = "androidx.room:room-compiler:$version"
        const val testing = "androidx.room:room-testing:$version"
    }

    const val preference = "androidx.preference:preference:1.1.1"

    object Test {
        private const val version = "1.4.0"
        const val runner = "androidx.test:runner:$version"
        const val rules = "androidx.test:rules:$version"
        object Espresso {
            private const val version = "3.4.0"
            const val core = "androidx.test.espresso:espresso-core:$version"
            const val contrib = "androidx.test.espresso:espresso-contrib:$version"
        }
        object Ext {
            const val junit = "androidx.test.ext:junit:1.1.3"
        }
    }
}

object Google {
    object Android {
        const val material = "com.google.android.material:material:1.5.0"
    }

    const val dagger = "com.google.dagger:dagger:${Dagger.version}"
    object Dagger {
        internal const val version = "2.40.5"
        const val compiler = "com.google.dagger:dagger-compiler:$version"
    }

    // Google's very impressive workaround for the following error:
    // Duplicate class com.google.common.util.concurrent.ListenableFuture found
    // in modules jetified-guava-19.0 (com.google.guava:guava:19.0) and
    // jetified-listenablefuture-1.0 (com.google.guava:listenablefuture:1.0)
    object Guava {
        const val workaround = "com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava"
    }
}

object Square {
    const val picasso = "com.squareup.picasso:picasso:2.8"
    const val leakcanary = "com.squareup.leakcanary:leakcanary-android:2.8.1"
}

object Tox4j {
    private const val version = "0.2.3"
    const val api = "org.toktok:tox4j-api_2.11:$version"
    const val c = "org.toktok:tox4j-c_2.11:$version"
    object Android {
        private const val version = "0.2.13"
        const val x86_64 = "org.toktok:tox4j-c_x86_64-linux-android:$version"
        const val arm = "org.toktok:tox4j-c_armv7a-linux-androideabi:$version"
        const val aarch64 = "org.toktok:tox4j-c_aarch64-linux-android:$version"
        const val i686 = "org.toktok:tox4j-c_i686-linux-android:$version"
    }
}

object Nayuki {
    const val qrcodegen = "io.nayuki:qrcodegen:1.7.0"
}

object Test {
    const val junit = "junit:junit:4.13.2"

    // Pinned to 1.11.0 as 1.12.0 bumps the minSdkVersion to 21.
    const val mockk = "io.mockk:mockk-android:1.11.0"
}
