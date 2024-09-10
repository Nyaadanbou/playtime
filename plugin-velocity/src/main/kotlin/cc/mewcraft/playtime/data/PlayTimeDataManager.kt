package cc.mewcraft.playtime.data

import cc.mewcraft.playtime.plugin
import cc.mewcraft.playtime.storage.PlayTimeDatabase
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.slf4j.Logger
import java.util.*
import java.util.concurrent.TimeUnit

interface PlayTimeDataManager {
    companion object {
        internal fun create(database: PlayTimeDatabase, logger: Logger): PlayTimeDataManager {
            return PlayTimeDataManagerImpl(database, logger)
        }
    }

    suspend fun getPlayTime(uniqueId: UUID): PlaytimeData

    suspend fun setPlayTime(uniqueId: UUID, timeData: PlaytimeData)

    suspend fun addPlayTime(uniqueId: UUID, timeData: PlaytimeData) {
        val currentData = getPlayTime(uniqueId)
        setPlayTime(uniqueId, currentData + timeData)
    }
}

private class PlayTimeDataManagerImpl(
    private val database: PlayTimeDatabase,
    private val logger: Logger
) : PlayTimeDataManager {
    private val dataCache: LoadingCache<UUID, Deferred<PlaytimeData>> = Caffeine.newBuilder()
        .expireAfterAccess(3, TimeUnit.MINUTES)
        .build { uniqueId ->
            plugin.scope.async {
                database.getPlayTime(uniqueId) ?: PlaytimeData()
            }
        }

    override suspend fun getPlayTime(uniqueId: UUID): PlaytimeData {
        logger.info("Getting play time for $uniqueId")
        return dataCache[uniqueId].await()
    }

    override suspend fun setPlayTime(uniqueId: UUID, timeData: PlaytimeData) {
        logger.info("Setting play time for $uniqueId to $timeData")
        database.setPlayTime(uniqueId, timeData)
        dataCache.invalidate(uniqueId)
    }
}