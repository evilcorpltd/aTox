import org.gradle.plugins.ide.idea.model.IdeaModule
import org.jetbrains.gradle.ext.ModuleSettings
import org.jetbrains.gradle.ext.PackagePrefixContainer

fun IdeaModule.settings(configure: ModuleSettings.() -> Unit) =
    (this as ExtensionAware).configure(configure)

val ModuleSettings.packagePrefix: PackagePrefixContainer
    get() = (this as ExtensionAware).the()

plugins {
    id(BuildPlugin.androidLibrary)

    id(BuildPlugin.kotlinAndroid)
    id(BuildPlugin.ksp)

    id(BuildPlugin.ideaExt)
}

apply<KtlintPlugin>()

android {
    compileSdk = AndroidSdk.targetVersion
    defaultConfig {
        minSdk = AndroidSdk.minVersion
        targetSdk = AndroidSdk.targetVersion
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
    implementation(JavaX.inject)
    api(AndroidX.Room.runtime)
    api(AndroidX.Room.ktx)
    ksp(AndroidX.Room.compiler)

    testImplementation(Test.junit)

    androidTestImplementation(AndroidX.Test.runner)
    androidTestImplementation(AndroidX.Test.Ext.junit)
    androidTestImplementation(AndroidX.Room.testing)
    androidTestImplementation(KotlinX.Coroutines.test) {
        // Conflicts with a lot of things due to having embedded "byte buddy" instead of depending on it.
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-debug")
    }
}
