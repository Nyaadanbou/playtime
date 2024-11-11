package cc.mewcraft.playtime.messaging

import cc.mewcraft.messenger.extension.getReqRespChannel
import cc.mewcraft.messenger.messaging.Messenger
import cc.mewcraft.messenger.messaging.reqresp.ReqRespChannel
import cc.mewcraft.playtime.data.PlaytimeData
import org.slf4j.Logger
import java.util.UUID

internal class SetPlaytimeRequestChannel(
    private val messenger: Messenger,
    private val logger: Logger,
) {
    private val channel: ReqRespChannel<SetPlaytimeRequest, SetPlaytimeResponse> = messenger.getReqRespChannel(PlaytimeConstants.SET_PLAYTIME_CHANNEL_ID)

    suspend fun setPlaytime(uniqueId: UUID, playtimeData: PlaytimeData) {
        val request = SetPlaytimeRequest(uniqueId, playtimeData)
        try {
            channel.request(request).await()
        } catch (e: Exception) {
            logger.warn("Failed to set playtime for player $uniqueId", e)
        }
    }
}