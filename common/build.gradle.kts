plugins {
    `maven-publish`
    `playtime-conventions`
}

version = "0.0.1"

dependencies {
    compileOnly(libs.messenger)
    compileOnly(libs.gson)
}

publishing {
    repositories {
        maven("https://repo.mewcraft.cc/private") {
            credentials {
                username = providers.gradleProperty("nyaadanbou.mavenUsername").orNull
                password = providers.gradleProperty("nyaadanbou.mavenPassword").orNull
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}