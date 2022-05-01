import org.gradle.plugins.ide.idea.model.IdeaModule
import org.jetbrains.gradle.ext.ModuleSettings
import org.jetbrains.gradle.ext.PackagePrefixContainer

fun IdeaModule.settings(configure: ModuleSettings.() -> Unit) =
    (this as ExtensionAware).configure(configure)

val ModuleSettings.packagePrefix: PackagePrefixContainer
    get() = (this as ExtensionAware).the()

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ideaExt)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKapt)
}

android {
    compileSdk = libs.versions.sdk.target.get().toInt()
    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
        targetSdk = libs.versions.sdk.target.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments.putAll(
                    mapOf("room.schemaLocation" to "$projectDir/schemas")
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
    api(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    implementation(libs.kodein.di)

    testImplementation(libs.test.junit.core)

    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.junit.ext)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test) {
        // Conflicts with a lot of things due to having embedded "byte buddy" instead of depending on it.
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-debug")
    }
}
