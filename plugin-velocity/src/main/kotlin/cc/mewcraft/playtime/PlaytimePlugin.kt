package cc.mewcraft.playtime

import cc.mewcraft.messenger.redis.RedisProvider
import cc.mewcraft.playtime.config.PlaytimeConfig
import cc.mewcraft.playtime.coroutine.VelocityCoroutineDispatcher
import cc.mewcraft.playtime.data.PlaytimeDataManager
import cc.mewcraft.playtime.event.PlaytimeReloadEvent
import cc.mewcraft.playtime.messaging.GetPlaytimeResponseChannel
import cc.mewcraft.playtime.messaging.SetPlaytimeResponseChannel
import cc.mewcraft.playtime.storage.PlaytimeDatabase
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

    private lateinit var playtime: Playtime
    private lateinit var task: PlaytimeTickTask

    private val coroutineDispatcher by lazy {
        VelocityCoroutineDispatcher(server.pluginManager.ensurePluginContainer(this), server)
    }

    private lateinit var getPlaytimeChannel: GetPlaytimeResponseChannel
    private lateinit var setPlaytimeChannel: SetPlaytimeResponseChannel

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

        val config = PlaytimeConfig(dataDirectory)
        config.load()

        val database = PlaytimeDatabase.mariadb(config)
        scope.launch {
            database.load()
        }

        val dataManager = PlaytimeDataManager.create(database, logger)

        task = PlaytimeTickTask(this, dataManager)
        task.start()

        getPlaytimeChannel = GetPlaytimeResponseChannel(RedisProvider.getRedis(), dataManager)
        setPlaytimeChannel = SetPlaytimeResponseChannel(RedisProvider.getRedis(), dataManager)

        playtime = PlaytimeImpl(dataManager)
        PlaytimeProvider.register(playtime)
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        PlaytimeProvider.unregister()
        instance = null
        dispose()
        logger.info("Playtime plugin unloaded.")
    }

    private fun dispose() {
        scope.coroutineContext.cancelChildren()
        scope.cancel()
    }

    fun reload() {
        server.eventManager.fire(PlaytimeReloadEvent())
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
