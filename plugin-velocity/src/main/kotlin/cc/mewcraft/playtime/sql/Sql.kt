package cc.mewcraft.playtime.sql

import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

/**
 * Represents an individual SQL datasource, created by the library.
 */
internal interface Sql {
    val hikari: HikariDataSource?
    val connection: Connection
    fun shutdown()
}