package foo.starred.nebulune.events

import foo.starred.athen.events.core.Event
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.level.chunk.LevelChunk

sealed class ClientChunkEvent {
    data class Load(
        val world: ClientLevel,
        val chunk: LevelChunk
    ) : Event()
}