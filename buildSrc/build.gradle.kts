plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()

    // 仓库提供: nyaadanbou version catalog, nyaadanbou conventions
    maven("https://repo.mewcraft.cc/private") {
        credentials {
            username = providers.gradleProperty("nyaadanbouUsername").getOrElse("")
            password = providers.gradleProperty("nyaadanbouPassword").getOrElse("")
        }
    }
}

dependencies {
    implementation(libs.plugin.shadow)
    implementation(libs.plugin.kotlin.jvm)
    implementation(libs.plugin.kotlin.kapt)
    implementation(libs.plugin.kotlin.serialization)
    implementation(libs.plugin.kotlin.atomicfu)
    implementation(libs.plugin.nyaadanbou.conventions)
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}