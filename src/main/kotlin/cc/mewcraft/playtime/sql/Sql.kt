package cc.mewcraft.playtime.sql

import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.SQLException

/**
 * Represents an individual SQL datasource, created by the library.
 */
interface Sql {
    val hikari: HikariDataSource?

    @get:Throws(SQLException::class)
    val connection: Connection

    fun close()
}