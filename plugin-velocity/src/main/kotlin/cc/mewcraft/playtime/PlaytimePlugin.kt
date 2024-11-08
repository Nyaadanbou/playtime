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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

internal val plugin: PlaytimePlugin
    get() = PlaytimePlugin.instance ?: throw IllegalStateException("instance not set yet")

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

    private lateinit var tickManager: PlaytimeTickManager
    private lateinit var dataManager: PlaytimeDataManager

    private lateinit var getPlaytimeChannel: GetPlaytimeResponseChannel
    private lateinit var setPlaytimeChannel: SetPlaytimeResponseChannel

    private lateinit var coroutineScope: CoroutineScope

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        instance = this

        // 初始化配置文件
        if (!dataDirectory.exists()) {
            dataDirectory.createDirectories()
        }
        val config = PlaytimeConfig(dataDirectory).also(PlaytimeConfig::load)

        // 初始化 data manager
        val database = PlaytimeDatabase.mariadb(config)
        coroutineScope = CoroutineScope(SupervisorJob() + createVirtualThreadExecutor().asCoroutineDispatcher())
        coroutineScope.launch { database.load() }
        dataManager = PlaytimeDataManager.create(database, logger)

        // 初始化 tick manager
        tickManager = PlaytimeTickManager(this, dataManager)
        tickManager.start()

        // 初始化 channel
        val redis = RedisProvider.redisProvider().getRedis()
        getPlaytimeChannel = GetPlaytimeResponseChannel(redis, dataManager)
        setPlaytimeChannel = SetPlaytimeResponseChannel(redis, dataManager)

        // 初始化 PlaytimeProvider
        PlaytimeProvider.register(this)
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        instance = null
        PlaytimeProvider.unregister()
        disposeCoroutineScope()
    }

    private fun disposeCoroutineScope() {
        coroutineScope.cancel()
    }

    fun reload() {
        server.eventManager.fire(PlaytimeReloadEvent())
    }

    fun <T> listen(eventType: Class<T>?, order: PostOrder?, action: EventHandler<T>?) {
        server.eventManager.register(this, eventType, order, action)
    }

    private fun createVirtualThreadExecutor(): ExecutorService {
        return Executors.newCachedThreadPool(
            ThreadFactoryBuilder().setNameFormat("playtime-main-%d").setThreadFactory(Thread.ofVirtual().factory()).build()
        )
    }

    override suspend fun getPlaytime(uuid: UUID): PlaytimeData {
        return dataManager.getPlayTime(uuid)
    }

    override suspend fun setPlaytime(uuid: UUID, playtimeData: PlaytimeData) {
        dataManager.setPlayTime(uuid, playtimeData)
    }
}