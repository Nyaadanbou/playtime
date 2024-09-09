plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("kapt") version "2.0.20"
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") {
            name = "papermc-repo"
        }
        maven("https://oss.sonatype.org/content/groups/public/") {
            name = "sonatype"
        }

        maven("https://repo.mewcraft.cc/private") {
            credentials {
                username = project.providers.gradleProperty("nyaadanbouUsername").getOrElse("")
                password = project.providers.gradleProperty("nyaadanbouPassword").getOrElse("")
            }
        }
    }

    group = "cc.mewcraft"
    version = "1.0.1-SNAPSHOT"
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-kapt")

    val targetJavaVersion = 21
    kotlin {
        jvmToolchain(targetJavaVersion)
    }
}