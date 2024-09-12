package cc.mewcraft.playtime.messaging

import cc.mewcraft.core.messenger.messaging.Messenger
import cc.mewcraft.core.messenger.messaging.conversation.ConversationChannel
import cc.mewcraft.core.messenger.messaging.conversation.ConversationReplyListener
import cc.mewcraft.core.messenger.messaging.extension.getConversationChannel
import cc.mewcraft.playtime.data.PlaytimeData
import kotlinx.coroutines.CompletableDeferred
import org.slf4j.Logger
import java.util.UUID
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class GetPlaytimeRequestChannel(
    private val logger: Logger,
    messenger: Messenger,
) {
    private val channel: ConversationChannel<GetPlaytimeRequest, GetPlaytimeResponse> = messenger.getConversationChannel(PlaytimeConstants.GET_PLAYTIME_CHANNEL_ID)

    suspend fun requestPlaytime(playerUniqueId: UUID): PlaytimeData? {
        val request = GetPlaytimeRequest(playerUniqueId)
        val response = CompletableDeferred<PlaytimeData?>()

        channel.buildMessage(request, 3.toDuration(DurationUnit.SECONDS))
            .onReply { reply ->
                response.complete(reply.data)
                ConversationReplyListener.RegistrationAction.STOP_LISTENING
            }
            .onTimeout {
                logger.warn("GetPlaytimeRequestChannel: requestPlaytime: Timeout")
                response.complete(null)
            }

        return response.await()
    }

    fun close() {
        channel.close()
    }
}