import groovy.lang.MissingPropertyException
import org.gradle.api.Project
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
