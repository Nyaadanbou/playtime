package cc.mewcraft.playtime

import cc.mewcraft.messenger.redis.RedisProvider
import cc.mewcraft.playtime.data.PlaytimeData
import cc.mewcraft.playtime.messaging.GetPlaytimeRequestChannel
import cc.mewcraft.playtime.messaging.SetPlaytimeRequestChannel
import com.github.shynixn.mccoroutine.bukkit.*
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import kotlinx.coroutines.launch
import net.kyori.adventure.identity.Identity
import org.bukkit.command.CommandSender
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Suppress("UnstableApiUsage")
internal class PlaytimePlugin : SuspendingJavaPlugin(), Playtime {
    private lateinit var getPlaytimeChannel: GetPlaytimeRequestChannel
    private lateinit var setPlaytimeChannel: SetPlaytimeRequestChannel

    private fun registerCommand() {
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands = event.registrar()
            commands.register(
                Commands.literal("playtime")
                    .executes { context ->
                        val sender = context.source.sender
                        val uuid = sender.uniqueId ?: return@executes 0
                        scope.launch(asyncDispatcher) {
                            val playtime = getPlaytime(uuid)
                            sender.sendMessage("Requested $playtime for $uuid")
                        }
                        1
                    }
                    .build()
            )
        }
    }

    private val CommandSender.uniqueId: UUID?
        get() = pointers().get(Identity.UUID).getOrNull()

    override fun onEnable() {
        val messenger = RedisProvider.redisProvider().getRedis()
        getPlaytimeChannel = GetPlaytimeRequestChannel(messenger, componentLogger)
        setPlaytimeChannel = SetPlaytimeRequestChannel(messenger, componentLogger)

        PlaytimeProvider.register(this)

        registerCommand()
    }

    override fun onDisable() {
        PlaytimeProvider.unregister()
    }

    override suspend fun getPlaytime(uuid: UUID): PlaytimeData? {
        return getPlaytimeChannel.getPlaytime(uuid)
    }

    override suspend fun setPlaytime(uuid: UUID, playtimeData: PlaytimeData) {
        setPlaytimeChannel.setPlaytime(uuid, playtimeData)
    }
}