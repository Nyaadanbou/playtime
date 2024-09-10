package cc.mewcraft.playtime.messaging

import cc.mewcraft.core.messenger.messaging.Messenger
import cc.mewcraft.core.messenger.messaging.conversation.ConversationChannel
import cc.mewcraft.core.messenger.messaging.conversation.toReply
import cc.mewcraft.core.messenger.messaging.extension.getConversationChannel
import cc.mewcraft.playtime.data.PlayTimeDataManager

internal class GetPlaytimeResponseChannel(
    messenger: Messenger,
    manager: PlayTimeDataManager,
) {
    private val channel: ConversationChannel<GetPlaytimeMessage, GetPlaytimeResponse> = messenger.getConversationChannel(ChannelSupport.GET_PLAYTIME)

    init {
        channel.newAgent onReceive@{ _, message ->
            GetPlaytimeResponse(
                message.conversationId,
                manager.getPlayTime(message.playerUniqueId)
            ).toReply()
        }
    }

    fun close() {
        channel.close()
    }
}