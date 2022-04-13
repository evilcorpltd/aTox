plugins {
    alias(libs.plugins.ideaExt)
    alias(libs.plugins.versions)
    id(BuildPlugin.kotlinAndroid) version kotlinVersion apply false
    id(BuildPlugin.kotlinKapt) version kotlinVersion apply false
}

buildscript {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(BuildPlugin.gradle)
    }
}

tasks.register("clean").configure {
    delete("build")
}
