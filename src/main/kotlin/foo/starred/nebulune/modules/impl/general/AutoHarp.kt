package foo.starred.nebulune.modules.impl.general

import foo.starred.athen.annotations.impl.LoadableAthenModule
import foo.starred.athen.annotations.impl.AthenModuleScope
import foo.starred.athen.api.location.island.impl.PresetSkyBlockIsland
import foo.starred.athen.config.dsl.impl.category.ConfigCategory
import foo.starred.athen.events.GuiEvent
import foo.starred.athen.events.TickEvent
import foo.starred.athen.modules.Module
import foo.starred.kontext.abstraction.gui.MinecraftGui
import foo.starred.kbus.extensions.runWhen
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.api.data.Observable
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items

@LoadableAthenModule
@AthenModuleScope(islands = [PresetSkyBlockIsland.THE_PARK, PresetSkyBlockIsland.PRIVATE_ISLAND])
object AutoHarp : Module(
    "Auto harp",
    "Automatically does Melody's Harp for you!",
    ConfigCategory.GENERAL
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
            //~ if >= 26.2 'client.screen' -> 'client.gui.screen()'
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
            MinecraftGui.click(screen.menu.containerId, slot, clickType = ContainerInput.CLONE)
        }.runWhen(bool)
    }
}