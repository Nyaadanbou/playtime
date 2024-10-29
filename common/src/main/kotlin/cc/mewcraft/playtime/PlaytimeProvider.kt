package cc.mewcraft.playtime

import org.jetbrains.annotations.ApiStatus

object PlaytimeProvider {
    private var instance: Playtime? = null

    /**
     * 获取 [Playtime] 实例
     *
     * @return [Playtime] 实例
     * @throws IllegalStateException 如果 [Playtime] 未初始化
     */
    fun get(): Playtime {
        return instance ?: throw IllegalStateException("Playtime is not initialized")
    }

    @ApiStatus.Internal
    fun register(instance: Playtime) {
        PlaytimeProvider.instance = instance
    }

    @ApiStatus.Internal
    fun unregister() {
        instance = null
    }
}