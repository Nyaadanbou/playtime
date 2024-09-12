plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    kotlin("plugin.atomicfu")
    id("com.gradleup.shadow")
    `maven-publish`
}

group = "cc.mewcraft"
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
                /*
                    由于我们依赖第三方插件 KotlinMC (Release: https://modrinth.com/plugin/kotlinmc)
                    因此我们不需要将 Kotlin 所有的运行时环境打包进我们自己的 JAR
                    因此大部分都是用的 compileOnly

                    关于是用 compileOnly 还是 implementation 的原因：
                    - compileOnly = Kotlin JAR 已提供运行时，因此编译时依赖就行，无需打包进 JAR
                    - implementation = Kotlin JAR 未提供运行时，因此不仅需要编译时依赖，还需要打包进 JAR
                */
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