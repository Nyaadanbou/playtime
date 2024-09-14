package cc.mewcraft.playtime

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

    internal fun register(instance: Playtime) {
        this.instance = instance
    }

    internal fun unregister() {
        this.instance = null
    }
}