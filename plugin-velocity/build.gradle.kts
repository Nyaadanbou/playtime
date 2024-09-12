plugins {
    id("playtime-conventions")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    compileOnly("cc.mewcraft.core.messenger:messenger-velocity:1.0-SNAPSHOT")
    api(project(":common"))

    kapt("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.4.1")
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
            artifactId = "playtime-velocity"
            from(components["java"])
        }
    }
}