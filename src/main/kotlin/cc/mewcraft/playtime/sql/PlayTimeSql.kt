package cc.mewcraft.playtime.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class PlayTimeSql(
    private val credentials: DatabaseCredentials,
) : Sql {
    override val hikari: HikariDataSource = buildSource()

    override val connection: Connection
        get() = hikari.connection

    override fun close() {
        hikari.close()
    }

    private fun buildSource(): HikariDataSource {
        val hikari = HikariConfig()

        hikari.poolName = "playtime-sql-" + PlayTimeSqlSupport.POOL_COUNTER.getAndIncrement()

        hikari.driverClassName = "org.mariadb.jdbc.Driver"
        hikari.jdbcUrl = "jdbc:mariadb://${credentials.address}:${credentials.port}/${credentials.database}"

        hikari.username = credentials.username
        hikari.password = credentials.password

        hikari.maximumPoolSize = PlayTimeSqlSupport.MAXIMUM_POOL_SIZE
        hikari.minimumIdle = PlayTimeSqlSupport.MINIMUM_IDLE

        hikari.maxLifetime = PlayTimeSqlSupport.MAX_LIFETIME
        hikari.connectionTimeout = PlayTimeSqlSupport.CONNECTION_TIMEOUT
        hikari.leakDetectionThreshold = PlayTimeSqlSupport.LEAK_DETECTION_THRESHOLD

        val properties: Map<String, String> = buildMap {
            // Ensure we use utf8 encoding
            put("useUnicode", "true")
            put("characterEncoding", "utf8") // https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration

            put("cachePrepStmts", "true")
            put("prepStmtCacheSize", "250")
            put("prepStmtCacheSqlLimit", "2048")
            put("useServerPrepStmts", "true")
            put("useLocalSessionState", "true")
            put("rewriteBatchedStatements", "true")
            put("cacheResultSetMetadata", "true")
            put("cacheServerConfiguration", "true")
            put("elideSetAutoCommits", "true")
            put("maintainTimeStats", "false")
            put("alwaysSendSetIsolation", "false")
            put("cacheCallableStmts", "true") // Set the driver level TCP socket timeout
            // See: https://github.com/brettwooldridge/HikariCP/wiki/Rapid-Recovery
            put("socketTimeout", 30.toDuration(DurationUnit.SECONDS).inWholeMilliseconds.toString())
        }

        for ((key, value) in properties) {
            hikari.addDataSourceProperty(key, value)
        }

        return HikariDataSource(hikari)
    }
}

private object PlayTimeSqlSupport {
    val POOL_COUNTER = AtomicInteger(0)

    // https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing
    val MAXIMUM_POOL_SIZE = (Runtime.getRuntime().availableProcessors() * 2) + 1
    val MINIMUM_IDLE = min(MAXIMUM_POOL_SIZE.toDouble(), 10.0).toInt()

    val MAX_LIFETIME = 30.toDuration(DurationUnit.MINUTES).inWholeMilliseconds
    val CONNECTION_TIMEOUT = 10.toDuration(DurationUnit.SECONDS).inWholeMilliseconds
    val LEAK_DETECTION_THRESHOLD = 10.toDuration(DurationUnit.SECONDS).inWholeMilliseconds
}