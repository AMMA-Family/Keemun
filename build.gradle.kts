buildscript {
    repositories {
        google()
        maven("https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath(BuildPlugin.gradle)
        classpath(BuildPlugin.kotlin)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
    }
}
