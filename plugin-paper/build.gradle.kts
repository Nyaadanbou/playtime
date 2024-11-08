import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("playtime-conventions")
    id("nyaadanbou-conventions.copy-jar")
    alias(libs.plugins.pluginyml.paper)
}

version = "1.0.0-SNAPSHOT"

dependencies {
    api(project(":common"))
    compileOnly(libs.paper)
    compileOnly(libs.messenger.common)

    implementation(libs.mccoroutine.bukkit.api) {
        exclude("org.jetbrains.kotlin")
    }
    implementation(libs.mccoroutine.bukkit.core) {
        exclude("org.jetbrains.kotlin")
    }
}

tasks {
    copyJar {
        environment = "paper"
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
        register("Messenger") {
            required = true
            load = PaperPluginDescription.RelativeLoadOrder.OMIT
        }
    }
}