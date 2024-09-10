package cc.mewcraft.playtime.sql

/**
 * Represents the credentials for a remote database.
 */
internal data class DatabaseCredentials(
    val address: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
    val tablePrefix: String
)