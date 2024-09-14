plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    kotlin("plugin.atomicfu")
    id("com.gradleup.shadow")
    `maven-publish`
}

group = "cc.mewcraft.playtime"
version = "1.0-SNAPSHOT"

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

java {
    withSourcesJar()
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)

    sourceSets {
        val main by getting {
            dependencies {
                compileOnly(kotlin("stdlib"))
                compileOnly(kotlin("reflect"))
                compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC.2")
                compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.9.0-RC.2") {
                    exclude("com.google.guava")
                    exclude("org.jetbrains.kotlin")
                    exclude("org.jetbrains.kotlinx")
                }
                compileOnly("org.jetbrains.kotlinx:atomicfu:0.25.0")
            }
        }

        val test by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC.2")
                implementation("org.jetbrains.kotlinx:atomicfu:0.25.0")
            }
        }
    }
}