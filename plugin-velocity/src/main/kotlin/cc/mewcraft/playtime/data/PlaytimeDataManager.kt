package cc.mewcraft.playtime.data

import cc.mewcraft.playtime.storage.PlaytimeDatabase
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import kotlinx.coroutines.*
import org.slf4j.Logger
import java.util.UUID
import java.util.concurrent.TimeUnit

internal fun PlaytimeDataManager(
    scope: CoroutineScope,
    logger: Logger,
    database: PlaytimeDatabase,
): PlaytimeDataManager {
    return PlaytimeDataManagerImpl(scope, logger, database)
}

internal interface PlaytimeDataManager {
    suspend fun getPlayTime(uniqueId: UUID): PlaytimeData

    suspend fun setPlayTime(uniqueId: UUID, timeData: PlaytimeData)

    suspend fun editPlaytime(uuid: UUID, block: PlaytimeData.() -> PlaytimeData) {
        val oldData = getPlayTime(uuid)
        val newData = oldData.block()
        setPlayTime(uuid, newData)
    }

    suspend fun shutdown()
}

private class PlaytimeDataManagerImpl(
    scope: CoroutineScope,
    private val logger: Logger,
    private val database: PlaytimeDatabase,
) : PlaytimeDataManager {
    private val cacheScope = scope + CoroutineName("playtime-cache")
    private val dataCache: LoadingCache<UUID, Deferred<PlaytimeData>> = Caffeine.newBuilder()
        .expireAfterAccess(3, TimeUnit.MINUTES)
        .build { uniqueId ->
            cacheScope.async { database.getPlayTime(uniqueId) ?: PlaytimeData() }
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

    override suspend fun shutdown() {
        cacheScope.cancel("Shutting down")
    }
}