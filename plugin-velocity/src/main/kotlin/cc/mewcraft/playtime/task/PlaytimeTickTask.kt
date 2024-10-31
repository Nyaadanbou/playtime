package cc.mewcraft.playtime.task

import cc.mewcraft.playtime.PlaytimePlugin
import cc.mewcraft.playtime.data.PlaytimeData
import cc.mewcraft.playtime.data.PlaytimeDataManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class PlaytimeTickTask(
    private val plugin: PlaytimePlugin,
    private val manager: PlaytimeDataManager,
) {
    companion object {
        private val ADD_TIME_DELAY_DURATION = 1.toDuration(DurationUnit.SECONDS)
        private val SAVE_DELAY_DURATION = 1.toDuration(DurationUnit.MINUTES)
    }

    private val server = plugin.server

    private var scheduledTask: Job? = null
    private var saveTask: Job? = null

    private val playTimeData: ConcurrentHashMap<UUID, Long> = ConcurrentHashMap()

    fun start() {
        scheduledTask = plugin.scope.launch {
            while (true) {
                val players = server.allPlayers
                for (player in players) {
                    val uuid = player.uniqueId
                    val time = playTimeData.getOrDefault(uuid, 0)
                    playTimeData[uuid] = time + ADD_TIME_DELAY_DURATION.inWholeMilliseconds
                }
                delay(ADD_TIME_DELAY_DURATION)
            }
        }

        saveTask = plugin.scope.launch {
            while (true) {
                for (playTimeDatum in playTimeData) {
                    manager.addPlayTime(playTimeDatum.key, PlaytimeData(playTimeDatum.value))
                }
                playTimeData.clear()
                delay(SAVE_DELAY_DURATION)
            }
        }
    }

    fun stop() {
        scheduledTask?.cancel()
    }
}