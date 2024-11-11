package cc.mewcraft.playtime.task

import cc.mewcraft.playtime.data.PlaytimeData
import cc.mewcraft.playtime.data.PlaytimeDataManager
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.*
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class PlaytimeTickManager(
    scope: CoroutineScope,
    private val server: ProxyServer,
    private val manager: PlaytimeDataManager,
) {
    companion object {
        private val ADD_TIME_DELAY = 1.toDuration(DurationUnit.SECONDS)
        private val SAVE_DELAY = 1.toDuration(DurationUnit.MINUTES)
    }

    private val playTimeData: ConcurrentHashMap<UUID, Long> = ConcurrentHashMap()

    // 构建一个专门的 coroutine scope 执行无期限的 tick 逻辑
    private val tickScope = scope + CoroutineName("playtime-tick")

    fun start() {
        tickScope.launch {
            while (isActive) {
                val players = server.allPlayers
                for (player in players) {
                    val uuid = player.uniqueId
                    val time = playTimeData.getOrDefault(uuid, 0)
                    playTimeData[uuid] = time + ADD_TIME_DELAY.inWholeMilliseconds
                }
                delay(ADD_TIME_DELAY)
            }
        }

        tickScope.launch {
            while (isActive) {
                for (playTimeDatum in playTimeData) {
                    manager.addPlayTime(playTimeDatum.key, PlaytimeData(playTimeDatum.value))
                }
                playTimeData.clear()
                delay(SAVE_DELAY)
            }
        }
    }

    fun stop() {
        tickScope.cancel("Shutting down")
    }
}