package cc.mewcraft.playtime.messaging

import cc.mewcraft.messenger.extension.getConversationChannel
import cc.mewcraft.messenger.messaging.Messenger
import cc.mewcraft.messenger.messaging.conversation.ConversationChannel
import cc.mewcraft.messenger.messaging.conversation.ConversationReplyListener
import cc.mewcraft.playtime.data.PlaytimeData
import org.slf4j.Logger
import java.util.UUID
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SetPlaytimeRequestChannel(
    private val messenger: Messenger,
    private val logger: Logger,
) {
    private val channel: ConversationChannel<SetPlaytimeRequest, SetPlaytimeResponse> = messenger.getConversationChannel(PlaytimeConstants.SET_PLAYTIME_CHANNEL_ID)

    suspend fun requestSetPlaytime(uniqueId: UUID, playtimeData: PlaytimeData) {
        val request = SetPlaytimeRequest(uniqueId, playtimeData)
        channel.buildMessage(request, 3.toDuration(DurationUnit.SECONDS))
            .onReply { _ -> ConversationReplyListener.RegistrationAction.STOP_LISTENING }
            .onTimeout { logger.warn("SetPlaytimeRequestChannel: requestPlaytime: timeout") }
            .sendAndAwait()
    }
}