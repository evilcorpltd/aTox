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
    id(BuildPlugin.kotlinKapt)

    id(BuildPlugin.ideaExt)
}

android {
    compileSdkVersion(AndroidSdk.targetVersion)
    defaultConfig {
        minSdkVersion(AndroidSdk.minVersion)
        targetSdkVersion(AndroidSdk.targetVersion)
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = mapOf(
                    "room.incremental" to "true",
                    "room.schemaLocation" to "$projectDir/schemas"
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
    compileOptions {
        sourceCompatibility = Java.version
        targetCompatibility = Java.version
    }
    kotlinOptions {
        jvmTarget = Java.version.toString()
    }
    lintOptions {
        isAbortOnError = true
        isWarningsAsErrors = true
    }
    sourceSets["main"].java.srcDir("src/main/kotlin")
    sourceSets["test"].java.srcDir("src/test/kotlin")
    sourceSets["androidTest"].assets.srcDir("$projectDir/schemas")
    sourceSets["androidTest"].java.srcDir("src/androidTest/kotlin")
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
    // Stdlib
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.javaxInject)

    // Lifecycle
    api(Libraries.lifecycleExtensions)

    // Room
    api(Libraries.roomRuntime)
    kapt(Libraries.roomCompiler)

    // Dagger
    implementation(Libraries.dagger)
    kapt(Libraries.daggerCompiler)

    testImplementation(Libraries.junit)

    androidTestImplementation(Libraries.runner)
    androidTestImplementation(Libraries.androidJUnit)
    androidTestImplementation(Libraries.roomTesting)
}
