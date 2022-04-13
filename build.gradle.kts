plugins {
    id(BuildPlugin.ideaExt) version BuildPlugin.ideaExtVersion
    id(BuildPlugin.versions) version BuildPlugin.versionsVersion
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
