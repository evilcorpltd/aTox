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

apply<KtlintPlugin>()

android {
    compileSdkVersion(AndroidSdk.targetVersion)
    defaultConfig {
        minSdkVersion(AndroidSdk.minVersion)
        targetSdkVersion(AndroidSdk.targetVersion)
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
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
        freeCompilerArgs = listOf(
            "-XXLanguage:+InlineClasses",
            "-Xuse-experimental=kotlinx.coroutines.ObsoleteCoroutinesApi"
        )
    }
    lintOptions {
        disable("InvalidPackage") // tox4j is still not really allowed on Android. :/
        // The macOS domain:lint task fails due to not guarding AudioRecord with permission checks in this module.
        // This doesn't fail locally, and use of the audio code is guarded in the UI in the aTox module.
        isAbortOnError = false
    }
    sourceSets["main"].java.srcDir("src/main/kotlin")
    sourceSets["test"].java.srcDir("src/test/kotlin")
    sourceSets["androidTest"].java.srcDir("src/androidTest/kotlin")
    packagingOptions {
        // Work around scala-compiler and scala-library (via tox4j) trying to place files in the
        // same place.
        exclude("rootdoc.txt")
    }
}

idea {
    module {
        settings {
            packagePrefix["src/main/kotlin"] = "ltd.evilcorp.domain"
            packagePrefix["src/test/kotlin"] = "ltd.evilcorp.domain"
            packagePrefix["src/androidTest/kotlin"] = "ltd.evilcorp.domain"
        }
    }
}

val needFixing: Configuration by configurations.creating
dependencies {
    needFixing(Tox4j.Android.x86_64)
    needFixing(Tox4j.Android.i686)
    needFixing(Tox4j.Android.arm)
    needFixing(Tox4j.Android.aarch64)
}

tasks.register("fixPaths") {
    needFixing.asFileTree.forEach { jar ->
        val arch = when {
            jar.name.contains("aarch64") -> "arm64-v8a"
            jar.name.contains("arm") -> "armeabi-v7a"
            jar.name.contains("i686") -> "x86"
            jar.name.contains("x86_64") -> "x86_64"
            else -> throw GradleException("Unknown arch")
        }
        File("domain/src/main/jniLibs/$arch").mkdirs()
        copy {
            from(zipTree(jar).files)
            into("src/main/jniLibs/$arch")
            include("*.so")
        }
    }
}

tasks.named("preBuild") { dependsOn("fixPaths") }

dependencies {
    implementation(project(":core"))

    implementation(JavaX.inject)
    implementation(KotlinX.Coroutines.core)
    api(Tox4j.api)
    implementation(Tox4j.c)

    testImplementation(Test.junit)
    androidTestImplementation(AndroidX.Test.runner)
    androidTestImplementation(AndroidX.Test.Ext.junit)
    androidTestImplementation(Test.mockk)
}
