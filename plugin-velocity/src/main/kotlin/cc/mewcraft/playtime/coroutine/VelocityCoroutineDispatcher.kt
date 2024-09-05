package cc.mewcraft.playtime.coroutine

import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

internal class VelocityCoroutineDispatcher(
    private val pluginContainer: PluginContainer,
    private val server: ProxyServer
) : CoroutineDispatcher() {
    /**
     * Returns `true` if the execution of the coroutine should be performed with [dispatch] method.
     * Multithreading in Velocity works by different threadPools where
     * it is not clear who scheduled a task. Dispatch task every time.
     */
    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return true
    }

    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        server.scheduler
            .buildTask(pluginContainer, block)
            .schedule()
    }
}