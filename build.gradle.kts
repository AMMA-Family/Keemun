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

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}