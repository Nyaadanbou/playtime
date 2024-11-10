package cc.mewcraft.playtime.messaging

import cc.mewcraft.messenger.extension.getReqRespChannel
import cc.mewcraft.messenger.messaging.Messenger
import cc.mewcraft.playtime.data.PlaytimeDataManager

internal class GetPlaytimeResponseChannel(
    messenger: Messenger,
    manager: PlaytimeDataManager,
) {
    init {
        messenger.getReqRespChannel<GetPlaytimeRequest, GetPlaytimeResponse>(
            PlaytimeConstants.GET_PLAYTIME_CHANNEL_ID
        ).responseHandler { request ->
            GetPlaytimeResponse(manager.getPlayTime(request.uuid))
        }
    }
}