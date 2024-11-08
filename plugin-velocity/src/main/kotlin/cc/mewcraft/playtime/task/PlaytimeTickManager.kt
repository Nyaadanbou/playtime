package cc.mewcraft.playtime.task

import cc.mewcraft.playtime.PlaytimePlugin
import cc.mewcraft.playtime.data.PlaytimeData
import cc.mewcraft.playtime.data.PlaytimeDataManager
import com.google.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.*
import java.util.UUID
import java.util.concurrent.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class PlaytimeTickManager(
    private val plugin: PlaytimePlugin,
    private val manager: PlaytimeDataManager,
) {
    companion object {
        private val ADD_TIME_DELAY = 1.toDuration(DurationUnit.SECONDS)
        private val SAVE_DELAY = 1.toDuration(DurationUnit.MINUTES)
    }

    private val server = plugin.server

    private val playTimeData: ConcurrentHashMap<UUID, Long> = ConcurrentHashMap()
    private val coroutineScope = CoroutineScope(SupervisorJob() + createVirtualThreadExecutor().asCoroutineDispatcher())

    private fun createVirtualThreadExecutor(): ExecutorService {
        return Executors.newCachedThreadPool(
            ThreadFactoryBuilder().setNameFormat("playtime-tick-%d").setThreadFactory(Thread.ofVirtual().factory()).build()
        )
    }

    fun start() {
        coroutineScope.launch {
            while (true) {
                val players = server.allPlayers
                for (player in players) {
                    val uuid = player.uniqueId
                    val time = playTimeData.getOrDefault(uuid, 0)
                    playTimeData[uuid] = time + ADD_TIME_DELAY.inWholeMilliseconds
                }
                delay(ADD_TIME_DELAY)
            }
        }

        coroutineScope.launch {
            while (true) {
                for (playTimeDatum in playTimeData) {
                    manager.addPlayTime(playTimeDatum.key, PlaytimeData(playTimeDatum.value))
                }
                playTimeData.clear()
                delay(SAVE_DELAY)
            }
        }
    }

    fun stop() {
        coroutineScope.cancel()
    }
}