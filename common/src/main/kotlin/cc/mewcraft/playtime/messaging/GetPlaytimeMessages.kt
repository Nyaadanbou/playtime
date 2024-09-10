package cc.mewcraft.playtime.messaging

import cc.mewcraft.core.messenger.messaging.conversation.ConversationMessage
import cc.mewcraft.playtime.data.PlaytimeData
import com.google.gson.annotations.SerializedName
import java.util.UUID

data class GetPlaytimeMessage(
    @SerializedName("player_uuid")
    val playerUniqueId: UUID,
    @SerializedName("id")
    override val conversationId: UUID = UUID.randomUUID(),
) : ConversationMessage

data class GetPlaytimeResponse(
    @SerializedName("id")
    override val conversationId: UUID,
    @SerializedName("data")
    val data: PlaytimeData
) : ConversationMessage