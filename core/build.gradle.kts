import org.jetbrains.gradle.ext.packagePrefix
import org.jetbrains.gradle.ext.settings

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ideaExt)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKapt)
}

android {
    namespace = "ltd.evilcorp.core"
    compileSdk = libs.versions.sdk.target.get().toInt()
    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
        targetSdk = libs.versions.sdk.target.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments.putAll(
                    mapOf("room.schemaLocation" to "$projectDir/schemas"),
                )
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    sourceSets["androidTest"].assets.srcDir("$projectDir/schemas")
}

idea {
    module {
        settings {
            packagePrefix["src/main/kotlin"] = "ltd.evilcorp.core"
            packagePrefix["src/test/kotlin"] = "ltd.evilcorp.core"
            packagePrefix["src/androidTest/kotlin"] = "ltd.evilcorp.core"
        }
    }
}

dependencies {
    implementation(libs.javax.inject)
    api(libs.kotlinx.coroutines.core)
    api(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    testImplementation(kotlin("test"))

    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.junit.ext)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test) {
        // Conflicts with a lot of things due to having embedded "byte buddy" instead of depending on it.
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-debug")
    }
}
