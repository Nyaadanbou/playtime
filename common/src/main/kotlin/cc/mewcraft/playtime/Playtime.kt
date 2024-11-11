package cc.mewcraft.playtime

import cc.mewcraft.playtime.data.PlaytimeData
import java.util.UUID

/**
 * 提供用于管理 [PlaytimeData] 的接口.
 *
 * 使用 [PlaytimeProvider] 来获取本接口的实例.
 */
interface Playtime {
    /**
     * 获取玩家的游戏时间数据.
     * 当获取到的数据为 `null`, 表示玩家还没有游戏时间数据.
     * 这可能是因为玩家刚进入服务器后还未到数据的刷新时间.
     *
     * @param uuid 玩家的 UUID
     */
    suspend fun getPlaytime(uuid: UUID): PlaytimeData?

    /**
     * 设置玩家的游戏时间数据, 将覆盖原有的数据.
     *
     * @param uuid 玩家的 UUID
     * @param playtimeData 新的游戏时间数据
     */
    suspend fun setPlaytime(uuid: UUID, playtimeData: PlaytimeData)
}