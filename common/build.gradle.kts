import java.net.URI

plugins {
    `maven-publish`
    `playtime-conventions`
}

version = "0.0.1"

repositories {
    maven {
        name = "nyaadanbouPrivate"
        url = URI("https://repo.mewcraft.cc/private")
        credentials(PasswordCredentials::class) // 需要配置 gradle.properties 的凭据
    }
}

dependencies {
    compileOnly(libs.messenger)
    compileOnly(libs.gson)
}

publishing {
    repositories {
        maven {
            name = "nyaadanbouReleases"
            url = URI("https://repo.mewcraft.cc/releases")
            credentials(PasswordCredentials::class) // 需要配置 gradle.properties 的凭据
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}