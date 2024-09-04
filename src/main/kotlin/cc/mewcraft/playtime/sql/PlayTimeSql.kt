package cc.mewcraft.playtime.sql

import com.google.common.collect.ImmutableMap
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

class PlayTimeSql(credentials: DatabaseCredentials) : Sql {
    private val source: HikariDataSource

    init {
        val hikari = HikariConfig()

        hikari.poolName = "playtime-sql-" + POOL_COUNTER.getAndIncrement()

        hikari.driverClassName = "org.mariadb.jdbc.Driver"
        hikari.jdbcUrl = "jdbc:mariadb://${credentials.address}:${credentials.port}/${credentials.database}"

        hikari.username = credentials.username
        hikari.password = credentials.password

        hikari.maximumPoolSize = MAXIMUM_POOL_SIZE
        hikari.minimumIdle = MINIMUM_IDLE

        hikari.maxLifetime = MAX_LIFETIME
        hikari.connectionTimeout = CONNECTION_TIMEOUT
        hikari.leakDetectionThreshold = LEAK_DETECTION_THRESHOLD

        val properties: Map<String, String> = ImmutableMap.builder<String, String>() // Ensure we use utf8 encoding
            .put("useUnicode", "true")
            .put("characterEncoding", "utf8") // https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration

            .put("cachePrepStmts", "true")
            .put("prepStmtCacheSize", "250")
            .put("prepStmtCacheSqlLimit", "2048")
            .put("useServerPrepStmts", "true")
            .put("useLocalSessionState", "true")
            .put("rewriteBatchedStatements", "true")
            .put("cacheResultSetMetadata", "true")
            .put("cacheServerConfiguration", "true")
            .put("elideSetAutoCommits", "true")
            .put("maintainTimeStats", "false")
            .put("alwaysSendSetIsolation", "false")
            .put("cacheCallableStmts", "true") // Set the driver level TCP socket timeout
            // See: https://github.com/brettwooldridge/HikariCP/wiki/Rapid-Recovery

            .put("socketTimeout", TimeUnit.SECONDS.toMillis(30).toString())
            .build()

        for ((key, value) in properties) {
            hikari.addDataSourceProperty(key, value)
        }

        this.source = HikariDataSource(hikari)
    }

    override val hikari: HikariDataSource
        get() = this.source

    @get:Throws(SQLException::class)
    override val connection: Connection
        get() = source.connection

    override fun close() {
        source.close()
    }

    companion object {
        private val POOL_COUNTER = AtomicInteger(0)

        // https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing
        private val MAXIMUM_POOL_SIZE = (Runtime.getRuntime().availableProcessors() * 2) + 1
        private val MINIMUM_IDLE = min(MAXIMUM_POOL_SIZE.toDouble(), 10.0).toInt()

        private val MAX_LIFETIME = TimeUnit.MINUTES.toMillis(30)
        private val CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(10)
        private val LEAK_DETECTION_THRESHOLD = TimeUnit.SECONDS.toMillis(10)
    }
}