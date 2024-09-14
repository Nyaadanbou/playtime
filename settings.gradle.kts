rootProject.name = "playtime"

include("plugin-velocity")
include("plugin-paper")
include("common")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}