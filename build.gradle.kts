buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath(BuildPlugin.gradle)
        classpath(BuildPlugin.kotlin)
    }
}

plugins {
    id("org.jetbrains.dokka") version "1.4.20"
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}