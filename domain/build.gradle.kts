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
        freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
    }
    lintOptions {
        isAbortOnError = true
        isWarningsAsErrors = true
        disable("InvalidPackage") // tox4j is still not really allowed on Android. :/
    }
    sourceSets["main"].java.srcDir("src/main/kotlin")
    sourceSets["test"].java.srcDir("src/test/kotlin")
    sourceSets["androidTest"].java.srcDir("src/androidTest/kotlin")
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
    needFixing(Libraries.toxcore_x86_64)
    needFixing(Libraries.toxcore_i686)
    needFixing(Libraries.toxcore_arm)
    needFixing(Libraries.toxcore_aarch64)
}

tasks.register("fixPaths") {
    needFixing.asFileTree.forEach { jar ->
        val arch = when {
            jar.name.contains("aarch64") -> "arm64_v8a"
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

dependencies {
    implementation(Libraries.kotlinStdLib)

    implementation(project(":core"))

    implementation(Libraries.javaxInject)

    implementation(Libraries.ktxCoroutinesCore)

    testImplementation(Libraries.junit)

    androidTestImplementation(Libraries.runner)
    androidTestImplementation(Libraries.androidJUnit)
    androidTestImplementation(Libraries.mockk)

    api(Libraries.tox4jApi)
    implementation(Libraries.tox4jC)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    // newSingleThreadContext
    kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.coroutines.ObsoleteCoroutinesApi")
}
