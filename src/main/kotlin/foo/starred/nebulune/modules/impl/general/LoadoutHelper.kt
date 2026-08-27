@file:Suppress("Unused")

package foo.starred.nebulune.modules.impl.general

import foo.starred.athen.annotations.Load
import foo.starred.athen.api.messaging.enums.MessagePrefixType
import foo.starred.athen.api.messaging.impl.MessagingAPI.mod
import foo.starred.athen.api.rendering.ui.text.vanilla.extensions.sizedText
import foo.starred.athen.api.scheduling.Scheduler
import foo.starred.athen.events.GuiEvent
import foo.starred.athen.events.InputEvent
import foo.starred.athen.events.PacketEvent
import foo.starred.athen.events.TickEvent
import foo.starred.athen.events.core.on
import foo.starred.athen.events.core.runWhen
import foo.starred.athen.mixin.accessors.KeyMappingAccessor
import foo.starred.athen.modules.impl.general.LoadoutKeybinds
import foo.starred.athen.utils.guiClick
import foo.starred.nebulune.utils.command
import foo.starred.snowbird.api.client
import foo.starred.snowbird.api.command
import foo.starred.snowbird.api.data.Observable.Companion.and
import foo.starred.snowbird.api.mainThread
import foo.starred.snowbird.api.scheduling.scheduler.extensions.clientTicks
import foo.starred.snowbird.utils.stripped
import net.minecraft.client.KeyMapping
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket

@Load
object LoadoutHelper {
    val autoClose by LoadoutKeybinds.config.switch("Auto close after use")
    private val autoEquip = LoadoutKeybinds.config.switch("Auto equip").unique("autoEquip")
    private val _unused by LoadoutKeybinds.config.information("Automatically equips the loadout slot without opening the gui. Use at your own risk.")
    private val moveEquip by LoadoutKeybinds.config.switch("Equip while moving")
    private val _unused0 by LoadoutKeybinds.config.information("Equip while moving increases your chances of being banned by a lot.")
    private val resetOpen by LoadoutKeybinds.config.switch("Reset on GUI open", true)
    private val equipDelay by LoadoutKeybinds.config.slider("Click delay", 1, 0, 8, "ticks")
    private val closeDelay by LoadoutKeybinds.config.slider("Close delay", 1, 0, 8, "ticks")
    private val delayVariance by LoadoutKeybinds.config.slider("Max delay variety", 1, 0, 5, "ticks")

    private val hud = LoadoutKeybinds.config.hud("Display text") {
        if (it) return@hud sizedText("Equipping §7[§c2§7]")
        if (!swapping) return@hud null
        val slot = slot0 ?: return@hud null
        sizedText("Equipping §7[§c${(slot.idx - 14) + 1}§7]")
    }

    private val all: List<KeyMapping>
        get() = listOf(
            client.options.keyUp,
            client.options.keyDown,
            client.options.keyLeft,
            client.options.keyRight,
            client.options.keyJump,
            client.options.keyShift
        )

    private var slot0: LoadoutKeybinds.LoadoutSlot? = null
    private var swapping: Boolean = false
    private var inMenu: Boolean = false
    private var id: Int = -1
    private var wait: Int = 0
    private var start: Long = 0

    init {
        command {
            "loadout" / int("slot", 1, 9) {
                if (!LoadoutKeybinds.enabled) return@int "Enable loadout keybinds!".mod(MessagePrefixType.ERROR)
                if (!autoEquip.value) return@int "Enable auto equip in loadout keybinds!".mod(MessagePrefixType.ERROR)

                val int = int("slot")
                val slot = LoadoutKeybinds.slots.find { it.index == int - 1 } ?: return@int

                slot0 = slot
                swapping = true
                id = -1
                start = System.currentTimeMillis()

                "loadout".command()
            }
        }

        on<InputEvent.Keyboard.Press> {
            //~ if >= 26.2 'client.screen' -> 'client.gui.screen()'
            if (client.screen != null) return@on

            val key = keyEvent.key

            if (!moveEquip && swapping) for (a in all) if ((a as KeyMappingAccessor).boundKey.value == key) return@on cancel()
            if (swapping) return@on

            val slot = LoadoutKeybinds.slots.find { it.value == key } ?: return@on

            slot0 = slot
            swapping = true
            id = -1
            start = System.currentTimeMillis()

            "loadout".command()
            cancel()
        }.runWhen(LoadoutKeybinds.observable and autoEquip.state)

        on<PacketEvent.Receive, ClientboundOpenScreenPacket> {
            if (!swapping) return@on
            if ("Loadout" !in title.stripped()) return@on
            val player = client.player ?: return@on

            mainThread {
                if (!moveEquip) for (a in all) a.isDown = false
                player.containerMenu = type.create(containerId, player.inventory)
            }

            id = containerId
            wait = equipDelay + (0..delayVariance).random()
            inMenu = true
            it.cancel()
        }.runWhen(LoadoutKeybinds.observable and autoEquip.state)

        on<PacketEvent.Receive, ClientboundContainerClosePacket> {
            reset()
        }.runWhen(LoadoutKeybinds.observable and autoEquip.state)

        on<PacketEvent.Send, ServerboundContainerClosePacket> {
            reset()
        }.runWhen(LoadoutKeybinds.observable and autoEquip.state)

        on<GuiEvent.Open.Container> {
            if (resetOpen) reset()
        }.runWhen(LoadoutKeybinds.observable and autoEquip.state)

        on<TickEvent.Client.Start> {
            if (!swapping) return@on
            if (System.currentTimeMillis() - start > 2000) return@on reset()
            if (!inMenu) return@on
            if (wait-- > 0) return@on

            val player = client.player ?: return@on
            val menu = player.containerMenu ?: return@on
            val slot = slot0 ?: return@on

            if (menu.containerId != id) return@on

            val mcSlot = menu.slots.getOrNull(slot.idx)?.takeIf { !it.item.isEmpty } ?: return@on
            if (!slot.equipped) guiClick(id, slot.idx)

            close()
            reset()
        }.runWhen(LoadoutKeybinds.observable and autoEquip.state)
    }

    @JvmStatic
    fun close(i: Int? = null) {
        val player = client.player ?: return

        Scheduler.schedule((i ?: (closeDelay + (0..delayVariance).random())).clientTicks) {
            player.closeContainer()
        }
    }

    private fun reset() {
        swapping = false
        inMenu = false
        slot0 = null
        id = -1
        wait = 0
        start = 0
    }
}