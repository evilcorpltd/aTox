plugins {
    id(BuildPlugin.ideaExt) version BuildPlugin.ideaExtVersion
    id(BuildPlugin.versions) version BuildPlugin.versionsVersion
}

buildscript {
    repositories {
        mavenLocal()
        google()
        jcenter()
        maven {
            url = uri("https://dl.bintray.com/toktok/maven")
        }
    }
    dependencies {
        classpath(BuildPlugin.gradle)
        classpath(BuildPlugin.kotlinGradle)
        classpath(BuildPlugin.gradleVersionsPlugin)
    }
}

allprojects {
    repositories {
        mavenLocal()
        google()
        jcenter()
        maven {
            url = uri("https://dl.bintray.com/toktok/maven")
        }
    }
}

tasks.register("clean").configure {
    delete("build")
}
