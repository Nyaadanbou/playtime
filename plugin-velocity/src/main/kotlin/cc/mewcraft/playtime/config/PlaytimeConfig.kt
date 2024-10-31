package cc.mewcraft.playtime.config

import cc.mewcraft.playtime.event.PlaytimeReloadEvent
import cc.mewcraft.playtime.plugin
import cc.mewcraft.playtime.sql.DatabaseCredentials
import cc.mewcraft.playtime.util.listen
import cc.mewcraft.playtime.util.reloadable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.lang.reflect.Type
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.outputStream

private const val CONFIG_FILE_NAME = "config.yml"

internal class PlaytimeConfig(
    dataDir: Path
) {
    private val path: Path = dataDir.resolve(CONFIG_FILE_NAME)

    init {
        plugin.listen<PlaytimeReloadEvent> { load() }
    }

    fun load() {
        if (!path.exists()) {
            initConfig()
        }
    }

    private val loader: YamlConfigurationLoader by reloadable {
        YamlConfigurationLoader.builder()
            .path(path)
            .nodeStyle(NodeStyle.BLOCK)
            .defaultOptions { options -> options.serializers { it.register(DatabaseCredentials::class.java, DatabaseCredentialsSerializer) } }
            .build()
    }

    private val root: ConfigurationNode by reloadable { loader.load() }

    val databaseCredentials: DatabaseCredentials by reloadable { root.node("database").krequire() }

    private fun initConfig() {
        val resource = plugin::class.java.getResourceAsStream("/$CONFIG_FILE_NAME") ?: throw IllegalStateException("Missing default config file")
        resource.use { input ->
            path.outputStream().use { output -> input.copyTo(output) }
        }
    }
}

private inline fun <reified T> ConfigurationNode.krequire(): T {
    return this.get(T::class.java) ?: throw IllegalStateException("Missing required value at '${this.path().joinToString(".")}'")
}

private object DatabaseCredentialsSerializer : TypeSerializer<DatabaseCredentials> {
    override fun deserialize(type: Type, node: ConfigurationNode): DatabaseCredentials {
        val host = node.node("host").krequire<String>()
        val port = node.node("port").krequire<Int>()
        val database = node.node("database").krequire<String>()
        val username = node.node("username").krequire<String>()
        val password = node.node("password").krequire<String>()
        val tablePrefix = node.node("table_prefix").krequire<String>()
        return DatabaseCredentials(host, port, database, username, password, tablePrefix)
    }

    override fun serialize(type: Type?, obj: DatabaseCredentials?, node: ConfigurationNode?) {
        throw UnsupportedOperationException("DatabaseCredentials is not serializable")
    }
}