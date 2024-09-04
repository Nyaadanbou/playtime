package cc.mewcraft.playtime.data

import java.time.Duration

@JvmInline
value class PlayTimeData(
    val playTime: Long = 0
) {
    operator fun plus(other: PlayTimeData): PlayTimeData {
        return PlayTimeData(playTime + other.playTime)
    }

    operator fun minus(other: PlayTimeData): PlayTimeData {
        return PlayTimeData(playTime - other.playTime)
    }

    operator fun plus(other: Duration): PlayTimeData {
        return PlayTimeData(playTime + other.toMillis())
    }

    operator fun minus(other: Duration): PlayTimeData {
        return PlayTimeData(playTime - other.toMillis())
    }
}
