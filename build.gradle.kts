buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath(BuildPlugin.gradle)
        classpath(BuildPlugin.kotlinGradle)
    }
}

plugins {
    id(BuildPlugin.download) version BuildPlugin.downloadVersion
    id(BuildPlugin.ideaExt) version BuildPlugin.ideaExtVersion
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

tasks.register("clean").configure {
    delete("build")
}
