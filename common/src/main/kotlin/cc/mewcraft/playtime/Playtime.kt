package cc.mewcraft.playtime

import cc.mewcraft.playtime.data.PlaytimeData
import java.util.*

/**
 * 提供获取玩家数据的接口.
 */
interface Playtime {
    /**
     * 获取玩家的游戏时间数据.
     *
     * 当获取到的数据为 null, 表示玩家没有游戏时间数据. (可能是玩家进入服务器未满刷新时间)
     *
     * @param uuid 玩家的 UUID.
     */
    suspend fun getPlaytime(uuid: UUID): PlaytimeData?

    /**
     * 设置玩家的游戏时间数据.
     *
     * @param uuid 玩家的 UUID.
     * @param playtimeData 要设置的玩家游戏时间数据.
     */
    suspend fun setPlaytime(uuid: UUID, playtimeData: PlaytimeData)
}