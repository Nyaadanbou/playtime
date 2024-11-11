package cc.mewcraft.playtime.messaging

import cc.mewcraft.playtime.data.PlaytimeData
import com.google.gson.annotations.SerializedName
import java.util.UUID

/* request side */

data class GetPlaytimeRequest(@SerializedName("player_uuid") val uuid: UUID)

data class GetPlaytimeResponse(@SerializedName("playtime_data") val data: PlaytimeData)


/* response side */

data class SetPlaytimeRequest(
    @SerializedName("player_uuid")
    val uniqueId: UUID,
    @SerializedName("playtime_data")
    val data: PlaytimeData,
)

data object SetPlaytimeResponse