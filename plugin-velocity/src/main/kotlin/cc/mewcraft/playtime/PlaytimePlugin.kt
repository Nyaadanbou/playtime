package cc.mewcraft.playtime

import cc.mewcraft.messenger.redis.RedisProvider
import cc.mewcraft.playtime.config.PlayTimeConfig
import cc.mewcraft.playtime.coroutine.VelocityCoroutineDispatcher
import cc.mewcraft.playtime.data.PlayTimeDataManager
import cc.mewcraft.playtime.event.PlayTimeReloadEvent
import cc.mewcraft.playtime.messaging.GetPlaytimeResponseChannel
import cc.mewcraft.playtime.storage.PlayTimeDatabase
import cc.mewcraft.playtime.task.PlaytimeTickTask
import com.google.inject.Inject
import com.velocitypowered.api.event.EventHandler
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.*
import org.slf4j.Logger
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

@Plugin(
    id = "playtime",
    name = "playtime",
    version = "1.0.0-SNAPSHOT", // 记得同步更新
    dependencies = [Dependency(id = "kotlin")]
)
internal class PlaytimePlugin @Inject constructor(
    val logger: Logger,
    val server: ProxyServer,
    @DataDirectory
    val dataDirectory: Path,
) {
    companion object {
        internal var instance: PlaytimePlugin? = null
    }

    private lateinit var task: PlaytimeTickTask

    private val coroutineDispatcher by lazy {
        VelocityCoroutineDispatcher(server.pluginManager.ensurePluginContainer(this), server)
    }

    private lateinit var channel: GetPlaytimeResponseChannel

    lateinit var scope: CoroutineScope
        private set

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        logger.info("Playtime plugin loaded.")
        instance = this
        scope = CoroutineScope(exceptionHandler()) + SupervisorJob() + coroutineDispatcher

        if (!dataDirectory.exists()) {
            dataDirectory.createDirectories()
        }

        val config = PlayTimeConfig(dataDirectory)
        config.load()

        val database = PlayTimeDatabase.mariadb(config)
        scope.launch {
            database.load()
        }

        val dataManager = PlayTimeDataManager.create(database, logger)

        task = PlaytimeTickTask(this, dataManager)
        task.start()

        channel = GetPlaytimeResponseChannel(RedisProvider.getRedis(), dataManager)
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        instance = null
        dispose()
        logger.info("Playtime plugin unloaded.")
    }

    private fun dispose() {
        channel.close()
        scope.coroutineContext.cancelChildren()
        scope.cancel()
    }

    fun reload() {
        server.eventManager.fire(PlayTimeReloadEvent())
    }

    fun <T> listen(eventType: Class<T>?, order: PostOrder?, action: EventHandler<T>?) {
        server.eventManager.register(this, eventType, order, action)
    }

    private fun exceptionHandler(): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, throwable ->
            logger.error("An exception occurred in the coroutine.", throwable)
        }
    }
}

internal val plugin: PlaytimePlugin
    get() = PlaytimePlugin.instance ?: throw IllegalStateException("Playtime plugin is not loaded.")
