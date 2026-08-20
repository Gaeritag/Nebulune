package foo.starred.nebulune.events.dispatcher

import foo.starred.athen.annotations.Priority
import foo.starred.nebulune.events.ClientChunkEvent
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents

@Priority
object GenericEventDispatcher {
    init {
        ClientChunkEvents.CHUNK_LOAD.register(ClientChunkEvents.Load { world, chunk ->
            ClientChunkEvent.Load(world, chunk).post()
        })
    }
}