package foo.starred.nebulune.events.dispatcher

import foo.starred.athen.annotations.impl.LoadableAthenModule
import foo.starred.nebulune.events.ClientChunkEvent
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents

@LoadableAthenModule
object GenericEventDispatcher {
    init {
        ClientChunkEvents.CHUNK_LOAD.register(ClientChunkEvents.Load { world, chunk ->
            ClientChunkEvent.Load(world, chunk).post()
        })
    }
}