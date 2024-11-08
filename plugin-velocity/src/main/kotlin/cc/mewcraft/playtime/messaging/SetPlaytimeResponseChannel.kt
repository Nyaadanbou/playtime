package cc.mewcraft.playtime.messaging

import cc.mewcraft.messenger.extension.getConversationChannel
import cc.mewcraft.messenger.messaging.Messenger
import cc.mewcraft.messenger.messaging.conversation.ConversationChannel
import cc.mewcraft.messenger.messaging.conversation.toReply
import cc.mewcraft.playtime.data.PlaytimeDataManager

internal class SetPlaytimeResponseChannel(
    messenger: Messenger,
    manager: PlaytimeDataManager,
) {
    private val channel: ConversationChannel<SetPlaytimeRequest, SetPlaytimeResponse> = messenger.getConversationChannel(PlaytimeConstants.SET_PLAYTIME_CHANNEL_ID)

    init {
        channel.newAgent { _, message ->
            manager.setPlayTime(message.uniqueId, message.data)
            SetPlaytimeResponse(message.conversationId).toReply()
        }
    }
}