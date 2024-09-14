plugins {
    `kotlin-dsl`
}

group = "cc.mewcraft.playtime"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.shadow)
    implementation(libs.kotlin.jvm)
    implementation(libs.kotlin.kapt)
    implementation(libs.kotlin.serialization)
    implementation(libs.kotlin.atomicfu)
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}