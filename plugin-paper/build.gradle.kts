import net.minecrell.pluginyml.paper.PaperPluginDescription.RelativeLoadOrder

plugins {
    id("playtime-conventions")
    id("nyaadanbou-conventions.copy-jar")
    alias(libs.plugins.pluginyml.paper)
}

version = "1.0.0-SNAPSHOT"

dependencies {
    api(project(":common"))
    compileOnly(libs.paper)
    compileOnly(libs.messenger.paper)

    implementation(libs.mccoroutine.bukkit.api) {
        exclude("org.jetbrains.kotlin")
    }
    implementation(libs.mccoroutine.bukkit.core) {
        exclude("org.jetbrains.kotlin")
    }
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
            artifactId = "paper"
            from(components["java"])
        }
    }
}

paper {
    main = "cc.mewcraft.playtime.PlaytimePlugin"
    name = "Playtime"
    version = "${project.version}"
    apiVersion = "1.21"
    author = "g2213swo"
    serverDependencies {
        register("Kotlin") {
            required = true
            load = RelativeLoadOrder.BEFORE
        }
    }
}