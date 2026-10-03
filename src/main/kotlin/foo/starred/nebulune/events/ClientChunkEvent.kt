package foo.starred.nebulune.events

import foo.starred.athen.events.core.AthenEvent
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.level.chunk.LevelChunk

sealed class ClientChunkEvent : AthenEvent() {
    data class Load(
        val world: ClientLevel,
        val chunk: LevelChunk
    ) : AthenEvent()
}