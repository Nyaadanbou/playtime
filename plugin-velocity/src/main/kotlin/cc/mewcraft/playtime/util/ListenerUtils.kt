package cc.mewcraft.playtime.util

import cc.mewcraft.playtime.Playtime
import com.velocitypowered.api.event.PostOrder

inline fun <reified T : Any> Playtime.listen(
    order: PostOrder = PostOrder.NORMAL,
    noinline action: (T) -> Unit,
) {
    listen(T::class.java, order, action)
}