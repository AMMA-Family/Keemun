@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("maven-publish")
    id("signing")
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

val localProperties = project.localProperties()

extra["signing.keyId"] = localProperties.getProperty("publication.signing.keyId")
extra["signing.password"] = localProperties.getProperty("publication.signing.password")
extra["signing.secretKeyRingFile"] = "$rootDir/${localProperties.getProperty("publication.signing.secretKeyRingFileName")}"

publishing {
    repositories {
        maven {
            name = "mavenCentral"
            setUrl(
                if(project.version.let { it as String }.endsWith("-SNAPSHOT"))
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                else
                    "https://s01.oss.sonatype.org/content/repositories/releases/"
            )

            credentials {
                username = localProperties.getProperty("publication.user.login")
                password = localProperties.getProperty("publication.user.password")
            }
        }
    }
}

signing {
    sign(publishing.publications)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing.publications.withType<MavenPublication>().all {
    this.groupId = publicationGroupId
    val artifactName: String = project.requireProperty(name = "publication.artifactId")
    this.artifactId = if (name.contains("kotlinMultiplatform")) {
        artifactName
    } else {
        "$artifactName-$name"
    }
    this.version = publicationVersionName

    // Stub javadoc.jar artifact
    artifact(javadocJar)

    pom {
        name.set(publicationGroupId)
        description.set(project.requireProperty("publication.description"))
        url.set(project.requireProperty("publication.url"))
        licenses {
            license {
                name.set(project.requireProperty("publication.license.name"))
                url.set(project.requireProperty("publication.license.url"))
            }
        }
        developers {
            developer {
                name.set(project.requireProperty("publication.developer.name"))
                email.set(project.requireProperty("publication.developer.email"))
                organization.set(project.requireProperty("publication.developer.email"))
                organizationUrl.set(project.requireProperty("publication.developer.email"))
            }
        }
        scm {
            connection.set(project.requireProperty("publication.scm.connection"))
            developerConnection.set(project.requireProperty("publication.scm.developerConnection"))
            url.set(project.requireProperty("publication.scm.url"))
        }
    }
}