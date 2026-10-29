package foo.starred.nebulune.modules.impl.dungeons

import foo.starred.athen.annotations.impl.LoadableAthenModule
import foo.starred.athen.annotations.impl.AthenModuleScope
import foo.starred.athen.api.location.island.impl.PresetSkyBlockIsland
import foo.starred.athen.api.scheduling.Scheduler
import foo.starred.athen.config.dsl.impl.category.ConfigCategory
import foo.starred.athen.events.GuiEvent
import foo.starred.athen.events.PacketEvent
import foo.starred.athen.modules.Module
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.api.format.extension.string.stripped
import foo.starred.kontext.api.scheduling.scheduler.extensions.clientTicks
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket

@LoadableAthenModule
@AthenModuleScope(islands = [PresetSkyBlockIsland.THE_CATACOMBS])
object ChestCloser : Module(
    "Chest closer",
    "Automatically closes chests.",
    ConfigCategory.DUNGEONS
) {
    private val mode by config.selector("Close mode", listOf("Auto", "Click"))

    private val minDelay by config.slider("Minimum delay", 0, 0, 5, "ticks")
    private val maxDelay by config.slider("Maximum delay", 1, 0, 5, "ticks")

    private val mouse by config.switch("Mouse", true)
    private val key by config.switch("Key", true)

    private val set = setOf("Chest", "Large Chest")

    init {
        on<PacketEvent.Receive, ClientboundOpenScreenPacket> {
            if (title.stripped() !in set) return@on
            if (mode != 0) return@on

            it.cancel()

            val r = (minDelay..maxDelay.coerceAtLeast(minDelay)).random()
            if (r == 0) return@on client.connection?.send(ServerboundContainerClosePacket(containerId)) ?: Unit

            Scheduler.schedule(r.clientTicks) {
                client.player?.closeContainer()
            }
        }

        on<GuiEvent.Input.Mouse.Press> {
            if (mode != 1) return@on
            if (!mouse) return@on

            //~ if >= 26.2 'client.screen' -> 'client.gui.screen()'
            val screen = client.screen as? AbstractContainerScreen<*> ?: return@on
            if (screen.title.stripped() !in set) return@on

            cancel()
            client.player?.closeContainer()
        }

        on<GuiEvent.Input.Key.Press> {
            if (mode != 1) return@on
            if (!key) return@on
            if (client.options.keyInventory.matches(keyEvent)) return@on

            //~ if >= 26.2 'client.screen' -> 'client.gui.screen()'
            val screen = client.screen as? AbstractContainerScreen<*> ?: return@on
            if (screen.title.stripped() !in set) return@on

            cancel()
            client.player?.closeContainer()
        }
    }
}