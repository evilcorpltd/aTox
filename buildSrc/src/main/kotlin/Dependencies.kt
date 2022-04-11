const val kotlinVersion = "1.6.20"

object BuildPlugin {
    private object Version {
        const val gradle = "7.1.3"
    }

    const val androidApplication = "com.android.application"
    const val androidLibrary = "com.android.library"
    const val kotlinAndroid = "kotlin-android"
    const val kotlinKapt = "kotlin-kapt"

    const val gradle = "com.android.tools.build:gradle:${Version.gradle}"
    const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"

    const val ideaExt = "org.jetbrains.gradle.plugin.idea-ext"
    const val ideaExtVersion = "1.1.4"

    const val versions = "com.github.ben-manes.versions"
    const val versionsVersion = "0.42.0"
    const val gradleVersionsPlugin = "com.github.ben-manes:gradle-versions-plugin:$versionsVersion"
}

object AndroidSdk {
    const val minVersion = 19
    const val targetVersion = 31
}
