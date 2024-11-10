package cc.mewcraft.playtime.messaging

import cc.mewcraft.messenger.extension.getReqRespChannel
import cc.mewcraft.messenger.messaging.Messenger
import cc.mewcraft.playtime.data.PlaytimeDataManager

internal class SetPlaytimeResponseChannel(
    messenger: Messenger,
    manager: PlaytimeDataManager,
) {
    init {
        messenger.getReqRespChannel<SetPlaytimeRequest, SetPlaytimeResponse>(
            PlaytimeConstants.SET_PLAYTIME_CHANNEL_ID
        ).responseHandler { request ->
            manager.setPlayTime(request.uniqueId, request.data)
            SetPlaytimeResponse
        }
    }
}