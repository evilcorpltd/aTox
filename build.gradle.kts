buildscript {
    repositories {
        google()
        jcenter()
        maven {
            url = uri("https://dl.bintray.com/toktok/maven")
        }
    }
    dependencies {
        classpath(BuildPlugin.gradle)
        classpath(BuildPlugin.kotlinGradle)
    }
}

plugins {
    id(BuildPlugin.ideaExt) version BuildPlugin.ideaExtVersion
}

allprojects {
    repositories {
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
