plugins {
    alias(libs.plugins.ideaExt)
    alias(libs.plugins.versions)
    id(BuildPlugin.kotlinAndroid) version kotlinVersion apply false
    id(BuildPlugin.kotlinKapt) version kotlinVersion apply false
    id(BuildPlugin.androidLibrary) version androidPluginVersion apply false
    id(BuildPlugin.androidApplication) version androidPluginVersion apply false
}

tasks.register("clean").configure {
    delete("build")
}
