plugins {
    id("playtime-conventions")
    id("nyaadanbou-conventions.copy-jar")
}

version = "1.0.0-SNAPSHOT"

dependencies {
    api(project(":common"))
    compileOnly(libs.velocity); kapt(libs.velocity)
    compileOnly(libs.messenger)

    implementation(libs.hikaricp)
    implementation(libs.jdbc.mariadb)
}

tasks {
    copyJar {
        environment = "velocity"
        jarFileName = "playtime-${project.version}.jar"
    }
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
            artifactId = "velocity"
            from(components["java"])
        }
    }
}