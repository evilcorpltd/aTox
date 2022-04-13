plugins {
    alias(libs.plugins.ideaExt)
    alias(libs.plugins.versions)
}

buildscript {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(BuildPlugin.gradle)
        classpath(BuildPlugin.kotlinGradle)
    }
}

tasks.register("clean").configure {
    delete("build")
}
