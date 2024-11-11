package cc.mewcraft.playtime.storage

import cc.mewcraft.playtime.config.PlaytimeConfig
import cc.mewcraft.playtime.data.PlaytimeData
import cc.mewcraft.playtime.sql.PlaytimeSql
import cc.mewcraft.playtime.sql.Sql
import cc.mewcraft.playtime.util.Resources
import cc.mewcraft.playtime.util.use
import kotlinx.coroutines.*
import java.sql.Connection
import java.util.UUID

private const val UUID_COLUMN = "uuid"
private const val PLAY_TIME_COLUMN = "play_time"

internal fun PlaytimeDatabase(
    scope: CoroutineScope, config: PlaytimeConfig,
): PlaytimeDatabase {
    return MariadbPlaytimeDatabase(scope, config)
}

internal interface PlaytimeDatabase {
    suspend fun load()

    suspend fun getPlayTime(uniqueId: UUID): PlaytimeData?

    suspend fun setPlayTime(uniqueId: UUID, timeData: PlaytimeData)

    suspend fun shutdown()
}

private class MariadbPlaytimeDatabase(
    scope: CoroutineScope,
    config: PlaytimeConfig,
) : PlaytimeDatabase {
    private val storageScope: CoroutineScope = scope + CoroutineName("playtime-storage")

    private val sql: Sql = PlaytimeSql(config.databaseCredentials)

    private val tablePrefix = config.databaseCredentials.tablePrefix

    private val timeTableName = "${tablePrefix}playtime"

    private val playTimeTableSetupQuery = """
        |CREATE TABLE IF NOT EXISTS $timeTableName (
        |    `$UUID_COLUMN` VARCHAR(36) PRIMARY KEY,
        |    `$PLAY_TIME_COLUMN` BIGINT NOT NULL
        |) DEFAULT CHARSET=utf8;
    """.trimMargin()

    override suspend fun load() {
        initTable()
    }

    private val connection: Connection
        get() = sql.connection.also { it.autoCommit = false }

    private fun <T> useAsync(block: suspend Resources.() -> T) = storageScope.async {
        use { block(this) }
    }

    override suspend fun getPlayTime(uniqueId: UUID): PlaytimeData? = useAsync {
        with(connection.use()) {
            val getTimeStatement = prepareStatement(
                """
                    |SELECT $PLAY_TIME_COLUMN
                    |FROM $timeTableName
                    |WHERE $UUID_COLUMN = ?;
                """.trimMargin()
            ).use()

            getTimeStatement.setString(1, uniqueId.toString())

            val result = getTimeStatement.executeQuery()
            if (result.next()) {
                return@useAsync PlaytimeData(result.getLong("play_time"))
            }
        }

        return@useAsync null

    }.await()

    override suspend fun setPlayTime(uniqueId: UUID, timeData: PlaytimeData): Unit = useAsync {
        with(connection.use()) {
            try {
                val setTimeStatement = prepareStatement(
                    """
                        |INSERT INTO $timeTableName ($UUID_COLUMN, $PLAY_TIME_COLUMN)
                        |VALUES (?, ?)
                        |ON DUPLICATE KEY UPDATE $PLAY_TIME_COLUMN = VALUES($PLAY_TIME_COLUMN);
                    """.trimMargin()
                ).use()
                setTimeStatement.setString(1, uniqueId.toString())
                setTimeStatement.setLong(2, timeData.playTime)
                setTimeStatement.execute()
                commit()
            } catch (e: Exception) {
                rollback()
                throw e
            }
        }
    }.await()

    private suspend fun initTable(): Unit = useAsync {
        with(connection.use()) {
            try {
                prepareStatement(playTimeTableSetupQuery).use().execute()
                commit()
            } catch (e: Exception) {
                rollback()
                throw e
            }
        }
    }.await()

    override suspend fun shutdown() {
        storageScope.launch { sql.shutdown() }.join()
        storageScope.cancel("Shutting down")
    }
}