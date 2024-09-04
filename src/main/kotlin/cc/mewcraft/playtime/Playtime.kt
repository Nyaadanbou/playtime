package cc.mewcraft.playtime

import cc.mewcraft.playtime.command.PlayTimeCommand
import cc.mewcraft.playtime.config.PlayTimeConfig
import cc.mewcraft.playtime.coroutine.VelocityCoroutineDispatcher
import cc.mewcraft.playtime.data.PlayTimeDataManager
import cc.mewcraft.playtime.event.PlayTimeReloadEvent
import cc.mewcraft.playtime.storage.PlayTimeDatabase
import cc.mewcraft.playtime.task.PlayTimingTask
import com.google.inject.Inject
import com.velocitypowered.api.event.EventHandler
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
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
    version = BuildConstants.VERSION
)
class Playtime @Inject constructor(
    val logger: Logger,
    val server: ProxyServer,
    @DataDirectory
    val dataDirectory: Path,
) {
    companion object {
        internal var instance: Playtime? = null
    }

    private lateinit var task: PlayTimingTask
    lateinit var scope: CoroutineScope
        private set

    private val velocityCoroutineDispatcher by lazy {
        VelocityCoroutineDispatcher(server.pluginManager.ensurePluginContainer(this), server)
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        logger.info("Playtime plugin loaded.")
        instance = this
        scope = CoroutineScope(exceptionHandler()) + SupervisorJob() + velocityCoroutineDispatcher

        if (!dataDirectory.exists()) {
            dataDirectory.createDirectories()
        }

        val config = PlayTimeConfig(dataDirectory)
        config.load()

        val database = PlayTimeDatabase.mariadb(config)
        scope.launch {
            database.load()
        }

        val manager = PlayTimeDataManager.create(database, logger)
        val mainCommand = PlayTimeCommand.create(server, manager)
        server.commandManager.register("playtime", mainCommand)

        task = PlayTimingTask(this, manager)
        task.start()
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        logger.info("Playtime plugin unloaded.")
        instance = null
        dispose()
    }

    private fun dispose() {
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

internal val plugin: Playtime
    get() = Playtime.instance ?: throw IllegalStateException("Playtime plugin is not loaded.")
