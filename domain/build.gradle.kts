plugins {
    id(BuildPlugin.androidLibrary)

    id(BuildPlugin.kotlinAndroid)
    id(BuildPlugin.kotlinKapt)
}

android {
    compileSdkVersion(AndroidSdk.targetVersion)
    defaultConfig {
        minSdkVersion(AndroidSdk.minVersion)
        targetSdkVersion(AndroidSdk.targetVersion)
        versionCode = 1
        versionName = "0.1.0"
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
        // TODO(robinlinden): Delete/update invalid packages
        disable("InvalidPackage")
    }
    sourceSets["main"].java.srcDir("src/main/kotlin")
    sourceSets["test"].java.srcDir("src/test/kotlin")
}

dependencies {
    api(fileTree("libs") { include("*.jar") })

    implementation(Libraries.kotlinStdLib)

    implementation(project(":core"))

    implementation(Libraries.javaxInject)

    implementation(Libraries.ktxCoroutinesCore)

    // For tox4j
    // TODO(robinlinden): Fix tox4j build so we can update the scala dependencies
    // noinspection GradleDependency
    implementation(Libraries.scalaLibrary)
    implementation(Libraries.scalaLogging)
    implementation(Libraries.scalapbRuntime)
    implementation(Libraries.scodecCore)

    testImplementation(Libraries.junit)
}

val files = listOf(
    "https://build.tox.chat/job/tox4j-api_build_android_multiarch_release/lastSuccessfulBuild/artifact/tox4j-api/target/scala-2.11/tox4j-api_2.11-0.1.2.jar" to "libs/tox4j-api-c.jar",
    "https://build.tox.chat/job/tox4j_build_android_arm64_release/lastSuccessfulBuild/artifact/artifacts/tox4j-c_2.11-0.1.2-SNAPSHOT.jar" to "libs/tox4j-c.jar",
    "https://build.tox.chat/job/tox4j_build_android_armel_release/lastSuccessfulBuild/artifact/artifacts/libtox4j-c.so" to "src/main/jniLibs/armeabi-v7a/libtox4j-c.so",
    "https://build.tox.chat/job/tox4j_build_android_armel_release/lastSuccessfulBuild/artifact/artifacts/libtox4j-c.so" to "src/main/jniLibs/armeabi/libtox4j-c.so",
    "https://build.tox.chat/job/tox4j_build_android_x86_release/lastSuccessfulBuild/artifact/artifacts/libtox4j-c.so" to "src/main/jniLibs/x86/libtox4j-c.so",
    "https://build.tox.chat/job/tox4j_build_android_arm64_release/lastSuccessfulBuild/artifact/artifacts/libtox4j-c.so" to "src/main/jniLibs/arm64-v8a/libtox4j-c.so",
    "https://build.tox.chat/job/tox4j_build_android_x86-64_release/lastSuccessfulBuild/artifact/artifacts/libtox4j-c.so" to "src/main/jniLibs/x86_64/libtox4j-c.so"
)

val fetchTask = task<DefaultTask>("fetchTox4j")

files.forEach { (url, dst) ->
    val taskName = "fetch_${dst.replace("/", "_")}"
    task<de.undercouch.gradle.tasks.download.Download>(taskName) {
        src(url)
        dest(dst)
        overwrite(false)
    }

    fetchTask.dependsOn(taskName)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn(fetchTask)

    // newSingleThreadContext
    kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.coroutines.ObsoleteCoroutinesApi")
}
