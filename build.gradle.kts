plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("kapt") version "2.0.20"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.8"
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
    }

    group = "cc.mewcraft"
    version = "1.0.1-SNAPSHOT"
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-kapt")
    apply(plugin = "org.jetbrains.gradle.plugin.idea-ext")

    val targetJavaVersion = 21
    kotlin {
        jvmToolchain(targetJavaVersion)
    }
}