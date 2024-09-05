package cc.mewcraft.playtime.sql

import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

/**
 * Represents an individual SQL datasource, created by the library.
 */
interface Sql {
    val hikari: HikariDataSource?

    val connection: Connection

    fun close()
}