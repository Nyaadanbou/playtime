plugins {
    id("playtime-conventions")
    id("nyaadanbou-conventions.copy-jar")
}

version = "1.0.0-SNAPSHOT"

dependencies {
    api(project(":common"))
    compileOnly(libs.velocity)
    kapt(libs.velocity)
    compileOnly(libs.messenger.velocity)

    implementation(libs.hikaricp)
    implementation(libs.jdbc.mariadb)
}

tasks {
    copyJar {
        jarName.set("playtime-${project.version}.jar")
    }
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
            artifactId = "velocity"
            from(components["java"])
        }
    }
}