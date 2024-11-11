package cc.mewcraft.playtime.messaging

import cc.mewcraft.messenger.extension.getReqRespChannel
import cc.mewcraft.messenger.messaging.Messenger
import cc.mewcraft.messenger.messaging.reqresp.ReqRespChannel
import cc.mewcraft.playtime.data.PlaytimeData
import org.slf4j.Logger
import java.util.UUID

internal class GetPlaytimeRequestChannel(
    private val messenger: Messenger,
    private val logger: Logger,
) {
    private val channel: ReqRespChannel<GetPlaytimeRequest, GetPlaytimeResponse> = messenger.getReqRespChannel(PlaytimeConstants.GET_PLAYTIME_CHANNEL_ID)

    suspend fun getPlaytime(playerUniqueId: UUID): PlaytimeData? {
        val request = GetPlaytimeRequest(playerUniqueId)
        val response = try {
            channel.request(request).await()
        } catch (e: Exception) {
            logger.warn("Failed to get playtime for player $playerUniqueId", e)
            null
        }
        return response?.data
    }
}