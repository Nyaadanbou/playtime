plugins {
    id("playtime-conventions")
}

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