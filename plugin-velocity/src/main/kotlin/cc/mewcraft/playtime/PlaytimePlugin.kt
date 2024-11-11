@file:Suppress("MemberVisibilityCanBePrivate")

package cc.mewcraft.playtime

import cc.mewcraft.messenger.redis.RedisProvider
import cc.mewcraft.playtime.config.PlaytimeConfig
import cc.mewcraft.playtime.data.PlaytimeData
import cc.mewcraft.playtime.data.PlaytimeDataManager
import cc.mewcraft.playtime.event.PlaytimeReloadEvent
import cc.mewcraft.playtime.messaging.GetPlaytimeResponseChannel
import cc.mewcraft.playtime.messaging.SetPlaytimeResponseChannel
import cc.mewcraft.playtime.storage.PlaytimeDatabase
import cc.mewcraft.playtime.task.PlaytimeTickManager
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.inject.Inject
import com.velocitypowered.api.event.*
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.*
import org.slf4j.Logger
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

internal val plugin: PlaytimePlugin
    get() = PlaytimePlugin.instance ?: error("instance is not initialized yet")

@Plugin(
    id = "playtime",
    name = "playtime",
    version = "1.0.0-SNAPSHOT", // 记得同步更新
    dependencies = [
        Dependency(id = "kotlin"),
        Dependency(id = "messenger")
    ],
)
internal class PlaytimePlugin
@Inject constructor(
    val logger: Logger,
    val server: ProxyServer,
    @DataDirectory
    val dataDirectory: Path,
) : Playtime {
    companion object {
        internal var instance: PlaytimePlugin? = null
    }

    private val mainScope: CoroutineScope = CoroutineScope(
        CoroutineName("playtime-main") + SupervisorJob() + createExecutor().asCoroutineDispatcher()
    )

    private lateinit var database: PlaytimeDatabase
    private lateinit var dataManager: PlaytimeDataManager
    private lateinit var tickManager: PlaytimeTickManager

    private lateinit var getPlaytimeChannel: GetPlaytimeResponseChannel
    private lateinit var setPlaytimeChannel: SetPlaytimeResponseChannel

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent): Unit = runBlocking {
        instance = this@PlaytimePlugin

        // 初始化配置文件
        if (!dataDirectory.exists()) {
            dataDirectory.createDirectories()
        }
        val config = PlaytimeConfig(dataDirectory).also(PlaytimeConfig::load)

        // 初始化 data manager
        database = PlaytimeDatabase(mainScope, config).also { db -> db.load() }
        dataManager = PlaytimeDataManager(mainScope, logger, database)

        // 初始化 tick manager
        tickManager = PlaytimeTickManager(mainScope, server, dataManager)
        tickManager.start()

        // 初始化 msg channel
        val redis = RedisProvider.redisProvider().getRedis()
        getPlaytimeChannel = GetPlaytimeResponseChannel(redis, dataManager)
        setPlaytimeChannel = SetPlaytimeResponseChannel(redis, dataManager)

        // 初始化 PlaytimeProvider
        PlaytimeProvider.register(this@PlaytimePlugin)
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent): Unit = runBlocking {
        tickManager.stop()
        dataManager.shutdown()
        database.shutdown()

        mainScope.cancel("Shutting down")

        PlaytimeProvider.unregister()
        instance = null
    }

    fun reload() {
        server.eventManager.fire(PlaytimeReloadEvent())
    }

    fun <T> listen(eventType: Class<T>?, order: PostOrder?, action: EventHandler<T>?) {
        server.eventManager.register(this, eventType, order, action)
    }

    override suspend fun getPlaytime(uuid: UUID): PlaytimeData {
        return dataManager.getPlayTime(uuid)
    }

    override suspend fun setPlaytime(uuid: UUID, playtimeData: PlaytimeData) {
        dataManager.setPlayTime(uuid, playtimeData)
    }

    override suspend fun editPlaytime(uuid: UUID, block: PlaytimeData.() -> PlaytimeData) {
        dataManager.editPlaytime(uuid, block)
    }

    private fun createExecutor(): Executor {
        return Executors.newCachedThreadPool(
            ThreadFactoryBuilder().setNameFormat("playtime-executor-%d").setThreadFactory(Thread.ofVirtual().factory()).build()
        )
    }
}