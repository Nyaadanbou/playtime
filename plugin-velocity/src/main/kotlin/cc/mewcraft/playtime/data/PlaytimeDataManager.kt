package cc.mewcraft.playtime.data

import cc.mewcraft.playtime.storage.PlaytimeDatabase
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.google.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.*
import org.slf4j.Logger
import java.util.UUID
import java.util.concurrent.*

interface PlaytimeDataManager {
    companion object {
        internal fun create(database: PlaytimeDatabase, logger: Logger): PlaytimeDataManager {
            return PlaytimeDataManagerImpl(database, logger)
        }
    }

    suspend fun getPlayTime(uniqueId: UUID): PlaytimeData

    suspend fun setPlayTime(uniqueId: UUID, timeData: PlaytimeData)

    suspend fun addPlayTime(uniqueId: UUID, timeData: PlaytimeData) {
        val currentData = getPlayTime(uniqueId)
        setPlayTime(uniqueId, currentData + timeData)
    }
}

private class PlaytimeDataManagerImpl(
    private val database: PlaytimeDatabase,
    private val logger: Logger,
) : PlaytimeDataManager {
    private val dataCache: LoadingCache<UUID, Deferred<PlaytimeData>> = Caffeine.newBuilder()
        .expireAfterAccess(3, TimeUnit.MINUTES)
        .build { uniqueId ->
            coroutineScope.async(createVirtualThreadExecutor().asCoroutineDispatcher()) {
                database.getPlayTime(uniqueId) ?: PlaytimeData()
            }
        }

    private val coroutineScope = CoroutineScope(SupervisorJob() + createVirtualThreadExecutor().asCoroutineDispatcher())

    private fun createVirtualThreadExecutor(): ExecutorService {
        return Executors.newCachedThreadPool(
            ThreadFactoryBuilder().setNameFormat("playtime-data-%d").setThreadFactory(Thread.ofVirtual().factory()).build()
        )
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