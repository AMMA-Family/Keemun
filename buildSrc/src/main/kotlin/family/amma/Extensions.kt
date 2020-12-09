import groovy.lang.MissingPropertyException
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.maven
import java.io.File
import java.util.Properties

fun Project.requireProperty(name: String): String =
    findProperty(name)?.toString() ?: throw MissingPropertyException("Not found property with name: $name")

fun Project.localProperties(): Properties {
    val local = Properties()
    val localProperties: File = rootProject.file("local.properties")
    if (localProperties.exists()) {
        localProperties.inputStream().use { local.load(it) }
    }
    return local
}

enum class PublicationType {
    Android,
    Mpp,
    JavaLib
}

fun PublishingExtension.configure(
    project: Project,
    groupId: String,
    versionName: String,
    artifactId: String,
    publicationType: PublicationType
) {
    repositories {
        maven(url = project.requireProperty("publication.repository.url")) {
            name = project.requireProperty(name = "publication.repository.name")
            credentials {
                val localProperties = project.localProperties()
                username = localProperties.getProperty("publication.user.login")
                password = localProperties.getProperty("publication.user.password")
            }
        }
    }

    fun MavenPublication.configure() {
        this.groupId = groupId
        this.artifactId = artifactId
        this.version = versionName

        pom {
            name.set(groupId)
            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }
        }
    }

    when (publicationType) {
        PublicationType.Mpp -> {
            //TODO в kotlin 1.4.10 эта строчка вызывает проблемы, зависимость не синкается.
            //publications.withType<MavenPublication>().configureEach(MavenPublication::configure)
        }

        PublicationType.Android ->
            publications {
                register("mavenPublish", MavenPublication::class.java) {
                    configure()
                    from(project.components.getByName("release"))
                }
            }

        PublicationType.JavaLib ->
            publications {
                create("mavenPublish", MavenPublication::class.java) {
                    configure()
                    from(project.components.getByName("java"))
                }
            }
    }
}
