package cc.mewcraft.playtime.messaging

import cc.mewcraft.messenger.messaging.Messenger
import cc.mewcraft.messenger.messaging.conversation.ConversationChannel
import cc.mewcraft.messenger.messaging.conversation.toReply
import cc.mewcraft.messenger.messaging.extension.getConversationChannel
import cc.mewcraft.playtime.data.PlaytimeDataManager

internal class GetPlaytimeResponseChannel(
    messenger: Messenger,
    manager: PlaytimeDataManager,
) {
    private val channel: ConversationChannel<GetPlaytimeRequest, GetPlaytimeResponse> = messenger.getConversationChannel(PlaytimeConstants.GET_PLAYTIME_CHANNEL_ID)

    init {
        channel.newAgent onReceive@{ _, message ->
            GetPlaytimeResponse(
                message.conversationId,
                manager.getPlayTime(message.playerUniqueId)
            ).toReply()
        }
    }
}