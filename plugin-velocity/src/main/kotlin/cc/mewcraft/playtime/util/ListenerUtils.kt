package cc.mewcraft.playtime.util

import cc.mewcraft.playtime.PlaytimePlugin
import com.velocitypowered.api.event.PostOrder

internal inline fun <reified T : Any> PlaytimePlugin.listen(
    order: PostOrder = PostOrder.NORMAL,
    noinline action: (T) -> Unit,
) {
    listen(T::class.java, order, action)
}