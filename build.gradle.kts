plugins {
    id(BuildPlugin.download) version BuildPlugin.downloadVersion
    id(BuildPlugin.ideaExt) version BuildPlugin.ideaExtVersion
    id(BuildPlugin.versions) version BuildPlugin.versionsVersion
}

buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath(BuildPlugin.gradle)
        classpath(BuildPlugin.kotlinGradle)
        classpath(BuildPlugin.gradleVersionsPlugin)
    }
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
