package cc.mewcraft.playtime.data

import kotlin.time.Duration

data class PlaytimeData(
    val playTime: Long = 0
) {
    operator fun plus(other: PlaytimeData): PlaytimeData {
        return PlaytimeData(playTime + other.playTime)
    }

    operator fun minus(other: PlaytimeData): PlaytimeData {
        return PlaytimeData(playTime - other.playTime)
    }

    operator fun plus(other: Duration): PlaytimeData {
        return PlaytimeData(playTime + other.inWholeMilliseconds)
    }

    operator fun minus(other: Duration): PlaytimeData {
        return PlaytimeData(playTime - other.inWholeMilliseconds)
    }
}
