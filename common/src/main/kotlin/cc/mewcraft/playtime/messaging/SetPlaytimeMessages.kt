package cc.mewcraft.playtime.messaging

import cc.mewcraft.messenger.messaging.conversation.ConversationMessage
import cc.mewcraft.playtime.data.PlaytimeData
import com.google.gson.annotations.SerializedName
import java.util.*

data class SetPlaytimeRequest(
    @SerializedName("uuid")
    val uniqueId: UUID,
    @SerializedName("data")
    val data: PlaytimeData,
    @SerializedName("id")
    override val conversationId: UUID = UUID.randomUUID(),
) : ConversationMessage

data class SetPlaytimeResponse(
    @SerializedName("id")
    override val conversationId: UUID,
) : ConversationMessage