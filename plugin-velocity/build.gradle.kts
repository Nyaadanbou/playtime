plugins {
    id("playtime-conventions")
}

dependencies {
    compileOnly(libs.velocity)
    compileOnly(libs.messenger.velocity)
    api(project(":common"))

    kapt(libs.velocity)

    implementation(libs.hikaricp)
    implementation(libs.jdbc.mariadb)
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