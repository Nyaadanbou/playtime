package cc.mewcraft.playtime.command

import cc.mewcraft.playtime.data.PlayTimeDataManager
import cc.mewcraft.playtime.plugin
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import java.util.*
import kotlin.jvm.optionals.getOrNull
import kotlin.system.measureTimeMillis

object PlayTimeCommand {
    fun create(server: ProxyServer, manager: PlayTimeDataManager): BrigadierCommand {
        val playTime = BrigadierCommand.literalArgumentBuilder("playtime")
            .requires { it.hasPermission("playtime.command.playtime") }
            .suspendExecutes { context ->
                val source = context.source
                val sourceUniqueId = source.uniqueId ?: return@suspendExecutes Command.SINGLE_SUCCESS
                val time = manager.getPlayTime(sourceUniqueId)
                if (time.playTime == 0L) {
                    source.sendMessage(Component.text("You have no play time"))
                    return@suspendExecutes Command.SINGLE_SUCCESS
                }
                source.sendMessage(Component.text("Your play time is $time"))
                Command.SINGLE_SUCCESS
            }
            .then(BrigadierCommand.requiredArgumentBuilder("reload", StringArgumentType.word())
                .suggests { _, builder ->
                    builder.suggest("reload")
                    builder.buildFuture()
                }
                .executes { context ->
                    val mills = measureTimeMillis {
                        plugin.reload()
                    }
                    context.source.sendMessage(Component.text("Reloaded in $mills ms"))
                    Command.SINGLE_SUCCESS
                }
            )

        return BrigadierCommand(playTime.build())
    }

    private val CommandSource.uniqueId: UUID?
        get() {
            return this.pointers().get(Identity.UUID).getOrNull()
        }

    private fun <S, T : ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.suspendExecutes(executor: suspend (context: CommandContext<S>) -> Int): T {
        return this.executes { context -> runBlocking { executor(context) } }
    }
}