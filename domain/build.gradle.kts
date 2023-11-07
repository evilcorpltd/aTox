@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "ltd.evilcorp.domain"
    compileSdk = libs.versions.sdk.target.get().toInt()
    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    packaging {
        // Work around scala-compiler and scala-library (via tox4j) trying to place files in the
        // same place.
        resources.excludes.add("rootdoc.txt")
    }
}

val needFixing: Configuration by configurations.creating
dependencies {
    needFixing(libs.tox4j.android.amd64)
    needFixing(libs.tox4j.android.i686)
    needFixing(libs.tox4j.android.arm)
    needFixing(libs.tox4j.android.aarch64)
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.javax.inject)
    api(libs.kotlinx.coroutines.core)
    api(libs.tox4j.api)
    implementation(libs.tox4j.c)

    testImplementation(kotlin("test"))
    androidTestImplementation(kotlin("test"))
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.junit.ext)
    androidTestImplementation(libs.google.guava.workaround)
    androidTestImplementation(libs.kotlinx.coroutines.test) {
        // Conflicts with a lot of things due to having embedded "byte buddy" instead of depending on it.A
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-debug")
    }
}
