plugins {
    id("playtime-conventions")
}

version = "1.0.0-SNAPSHOT"

dependencies {
    compileOnly(libs.messenger.common)
    compileOnly(libs.gson)
}

publishing {
    repositories {
        maven {
            name = "nyaadanbou"
            url = uri("https://repo.mewcraft.cc/private")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}