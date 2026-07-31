package foo.starred.nebulune.modules.impl.general

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.location.SkyBlockIsland
import foo.starred.athen.config.Category
import foo.starred.athen.events.GuiEvent
import foo.starred.athen.events.TickEvent
import foo.starred.athen.events.core.runWhen
import foo.starred.athen.modules.Module
import foo.starred.athen.utils.guiClick
import foo.starred.snowbird.api.client
import foo.starred.snowbird.handlers.Observable
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items

@Load
@OnlyIn(islands = [SkyBlockIsland.THE_PARK, SkyBlockIsland.PRIVATE_ISLAND])
object AutoHarp : Module(
    "Auto harp",
    "Automatically does Melody's Harp for you!",
    Category.GENERAL
) {
    private var bool: Observable<Boolean> = Observable(false)
    private var hash: Int = 0

    init {
        on<GuiEvent.Open.Container> {
            if (!screen.title.string.startsWith("Harp - ")) return@on
            if (screen.menu.type != MenuType.GENERIC_9x6) return@on

            bool.value = true
        }

        on<GuiEvent.Close.Any> {
            bool.value = false
        }.runWhen(bool)

        on<TickEvent.Client.Start> {
            val screen = client.screen as? AbstractContainerScreen<*> ?: return@on
            val slots = screen.menu.slots

            var hash0 = 0
            var slot = -1

            for (i in 37..43) {
                val b0 = slots.getOrNull(i)?.item?.item == Items.QUARTZ_BLOCK
                hash0 = (hash0 shl 1) or if (b0) 1 else 0
                if (slot == -1 && b0) slot = i
            }

            if (hash == hash0) return@on
            hash = hash0

            if (slot == -1) return@on
            guiClick(screen.menu.containerId, slot, clickType = ClickType.CLONE)
        }.runWhen(bool)
    }
}