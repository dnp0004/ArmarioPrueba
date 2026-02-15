pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // --- ASÍ SE PONE JITPACK EN KOTLIN (.kts) ---
        maven { url = uri("https://jitpack.io") }
        // --------------------------------------------
    }
}

rootProject.name = "ArmarioCamara"
include(":app")