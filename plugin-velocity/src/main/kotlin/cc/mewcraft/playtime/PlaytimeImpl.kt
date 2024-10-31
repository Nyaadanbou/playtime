package cc.mewcraft.playtime

import cc.mewcraft.playtime.data.PlaytimeDataManager
import cc.mewcraft.playtime.data.PlaytimeData
import java.util.*

class PlaytimeImpl(
    private val dataManager: PlaytimeDataManager
) : Playtime {
    override suspend fun getPlaytime(uuid: UUID): PlaytimeData {
        return dataManager.getPlayTime(uuid)
    }

    override suspend fun setPlaytime(uuid: UUID, playtimeData: PlaytimeData) {
        dataManager.setPlayTime(uuid, playtimeData)
    }
}