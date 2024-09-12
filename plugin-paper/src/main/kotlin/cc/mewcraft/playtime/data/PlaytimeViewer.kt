package cc.mewcraft.playtime.data

import cc.mewcraft.playtime.plugin
import com.github.shynixn.mccoroutine.bukkit.scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.util.*

object PlaytimeViewer {
    suspend fun viewPlaytime(uniqueId: UUID): PlaytimeData? {
        return plugin.scope.async(Dispatchers.IO) {
            plugin.channel.requestPlaytime(uniqueId)
        }.await()
    }
}