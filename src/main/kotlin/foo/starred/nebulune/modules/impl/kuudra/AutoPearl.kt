package foo.starred.nebulune.modules.impl.kuudra

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.kuudra.KuudraAPI
import foo.starred.athen.api.kuudra.enums.KuudraPhase
import foo.starred.athen.api.location.SkyBlockIsland
import foo.starred.athen.api.scheduling.Scheduler
import foo.starred.athen.config.Category
import foo.starred.athen.events.InputEvent
import foo.starred.athen.modules.Module
import foo.starred.nebulune.mixin.accessors.InventoryAccessor
import foo.starred.nebulune.utils.rightClick
import foo.starred.snowbird.api.client
import foo.starred.snowbird.handlers.time.client
import foo.starred.snowbird.handlers.time.start
import foo.starred.snowbird.utils.stripped
import net.minecraft.world.item.Items

@Load
@OnlyIn(islands = [SkyBlockIsland.KUUDRA])
object AutoPearl : Module(
    "Auto pearl",
    "Automatically throws a pearl if you right click when holding a supply.",
    Category.KUUDRA
) {
    private val click by config.slider("Click delay", 1, 1, 2, "ticks")

    init {
        on<InputEvent.Mouse.Press> {
            //~ if >= 26.2 'client.screen' -> 'client.gui.screen()'
            if (client.screen != null) return@on
            if (KuudraAPI.phase != KuudraPhase.Supply) return@on

            val player = client.player ?: return@on
            val held = player.mainHandItem.takeIf { !it.isEmpty } ?: return@on
            if (held.displayName.stripped() != "Elle's Supplies") return@on

            cancel()
            (player.inventory as InventoryAccessor).selectedSlot = fn() ?: return@on
            Scheduler.schedule(click.client.start, ::rightClick)
        }
    }

    private fun fn(): Int? {
        val player = client.player ?: return null
        val inventory = player.inventory

        for (i in 0..8) {
            val it = inventory.getItem(i).takeIf { !it.isEmpty } ?: continue
            if (it.item != Items.ENDER_PEARL) continue

            return i
        }

        return null
    }
}