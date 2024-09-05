package cc.mewcraft.playtime.storage

import cc.mewcraft.playtime.config.PlayTimeConfig
import cc.mewcraft.playtime.data.PlayTimeData
import cc.mewcraft.playtime.sql.PlayTimeSql
import cc.mewcraft.playtime.sql.Sql
import cc.mewcraft.playtime.util.use
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.util.*

private const val UUID_COLUMN = "uuid"
private const val PLAY_TIME_COLUMN = "play_time"

internal interface PlayTimeDatabase {
    companion object {
        fun mariadb(config: PlayTimeConfig): PlayTimeDatabase {
            return MariadbPlayTimeDatabase(config)
        }
    }

    suspend fun load()

    suspend fun getPlayTime(uniqueId: UUID): PlayTimeData?

    suspend fun setPlayTime(uniqueId: UUID, timeData: PlayTimeData)

    suspend fun close()
}

private class MariadbPlayTimeDatabase(
    config: PlayTimeConfig,
) : PlayTimeDatabase {
    private val sql: Sql = PlayTimeSql(config.databaseCredentials)

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

    override suspend fun getPlayTime(uniqueId: UUID): PlayTimeData? = withContext(Dispatchers.IO) {
        use {
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
                    return@withContext PlayTimeData(result.getLong("play_time"))
                }
            }
            return@withContext null
        }
    }

    override suspend fun setPlayTime(uniqueId: UUID, timeData: PlayTimeData) = withContext(Dispatchers.IO) {
        use {
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
        }
    }

    private suspend fun initTable() = withContext(Dispatchers.IO) {
        use {
            with(connection.use()) {
                try {
                    prepareStatement(playTimeTableSetupQuery).use().execute()

                    commit()
                } catch (e: Exception) {
                    rollback()
                    throw e
                }
            }
        }
    }

    override suspend fun close() = withContext(Dispatchers.IO) {
        sql.close()
    }
}