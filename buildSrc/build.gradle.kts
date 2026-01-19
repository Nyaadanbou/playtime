import java.net.URI

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()

    maven {
        name = "nyaadanbouPrivate"
        url = URI("https://repo.mewcraft.cc/private")
        credentials(PasswordCredentials::class) // 需要配置 gradle.properties 的凭据
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