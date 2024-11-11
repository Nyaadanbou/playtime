import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    `playtime-conventions`
    `nyaadanbou-conventions`.`copy-jar`
    alias(libs.plugins.pluginyml.paper)
}

version = "1.0.0-SNAPSHOT"

dependencies {
    api(project(":common"))
    compileOnly(libs.paper)
    compileOnly(libs.messenger)

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