@file:Suppress("UnstableApiUsage")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "playtime"

include("common")
include("plugin-paper")
include("plugin-velocity")
