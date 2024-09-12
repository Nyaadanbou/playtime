plugins {
    id("playtime-conventions")
}

dependencies {
    compileOnly("cc.mewcraft.core.messenger:common:1.0-SNAPSHOT")
    compileOnly("com.google.code.gson:gson:2.10.1")
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