const val kotlinVersion = "1.6.20"

object BuildPlugin {
    private object Version {
        const val gradle = "7.1.3"
    }

    const val androidApplication = "com.android.application"
    const val androidLibrary = "com.android.library"
    const val kotlinAndroid = "org.jetbrains.kotlin.android"
    const val kotlinKapt = "org.jetbrains.kotlin.kapt"

    const val gradle = "com.android.tools.build:gradle:${Version.gradle}"
}

object AndroidSdk {
    const val minVersion = 19
    const val targetVersion = 31
}
