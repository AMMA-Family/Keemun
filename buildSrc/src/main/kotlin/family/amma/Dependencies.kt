@file:JvmMultifileClass

private const val kotlinVersion = "1.6.21"

object BuildPlugin {
    const val gradle = "com.android.tools.build:gradle:7.2.1"
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
}

object Dependency {
    object AndroidX {
        const val fragment = "androidx.fragment:fragment-ktx:1.4.1"
    }

    object Kotlin {
        object X {
            object Coroutines {
                private const val version = "1.6.2"

                private const val mainPath = "org.jetbrains.kotlinx:kotlinx-coroutines"
                const val core = "$mainPath-core:$version"
                const val android = "$mainPath-android:$version"
                const val test = "$mainPath-test:$version"
            }
        }
    }
}
