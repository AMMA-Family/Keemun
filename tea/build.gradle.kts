@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka")
}

kotlin {
    android {
        publishLibraryVariants("debug", "release")
        publishLibraryVariantsGroupedByFlavor = true
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))

                // Coroutines
                implementation(Dependency.Kotlin.X.Coroutines.core)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))

                // Coroutines
                implementation(Dependency.Kotlin.X.Coroutines.android)

                // Android
                implementation(Dependency.AndroidX.fragment)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))

                // Coroutines
                implementation(Dependency.Kotlin.X.Coroutines.test)
            }
        }

        val androidTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }

        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = AndroidProject.jvmTarget
    }
}

android {
    compileSdkVersion(AndroidProject.compileSdkVersion)
    defaultConfig {
        minSdkVersion(AndroidProject.minSdkVersion)
        targetSdkVersion(AndroidProject.targetSdkVersion)
    }

    sourceSets {
        val main by getting {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            java.srcDirs("src/androidMain/kotlin")
        }
    }

    testOptions.unitTests.isIncludeAndroidResources = true
}

val publicationGroupId: String = project.requireProperty(name = "publication.groupId")
val publicationVersionName: String = project.requireProperty(name = "publication.versionName")

group = publicationGroupId
version = publicationVersionName

extra["signing.keyId"] = project.localProperties().getProperty("publication.signing.keyId")
extra["signing.password"] = project.localProperties().getProperty("publication.signing.password")
extra["signing.secretKeyRingFile"] = "$rootDir/${project.localProperties().getProperty("publication.signing.secretKeyRingFileName")}"

signing {
    isRequired = true
    sign(publishing.publications)
}

val javadocJar by tasks.registering(Jar::class) { archiveClassifier.set("javadoc") }

publishing {
    configure(
        project = project,
        groupId = publicationGroupId,
        artifactId = project.requireProperty(name = "publication.artifactId"),
        versionName = publicationVersionName,
        publicationType = PublicationType.Mpp
    )
}
