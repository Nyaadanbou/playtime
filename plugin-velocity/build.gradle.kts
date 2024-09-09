plugins {
    id("com.gradleup.shadow") version "8.3.0"
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    compileOnly("cc.mewcraft.core.messenger:messenger-velocity:1.0-SNAPSHOT")

    kapt("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC.2")

    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.4.1")
}
