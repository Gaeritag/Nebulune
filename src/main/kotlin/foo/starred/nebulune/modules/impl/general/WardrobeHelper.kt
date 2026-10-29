@file:Suppress("Unused")

package foo.starred.nebulune.modules.impl.general

import foo.starred.athen.annotations.impl.LoadableAthenModule
import foo.starred.athen.api.messaging.enums.MessagePrefixType
import foo.starred.athen.api.messaging.impl.MessagingAPI.mod
import foo.starred.kontext.abstraction.text.measurer.MinecraftFontMeasurer
import foo.starred.kontext.abstraction.text.renderer.MinecraftFontRenderer
import foo.starred.athen.api.scheduling.Scheduler
import foo.starred.athen.api.scheduling.Ticking
import foo.starred.athen.events.GuiEvent
import foo.starred.athen.events.InputEvent
import foo.starred.athen.events.PacketEvent
import foo.starred.athen.events.TickEvent
import foo.starred.athen.events.core.on
import foo.starred.athen.modules.impl.general.WardrobeKeybinds
import foo.starred.kontext.abstraction.gui.MinecraftGui
import foo.starred.kbus.extensions.runWhen
import foo.starred.nebulune.accessors.IWardrobeSlot
import foo.starred.nebulune.mixin.accessors.WardrobeKeybindsAccessor
import foo.starred.nebulune.utils.command
import foo.starred.kontext.abstraction.chat.command.MinecraftCommands
import foo.starred.kontext.abstraction.client.extension.boundKey
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.abstraction.client.extension.mainThread
import foo.starred.kontext.abstraction.input.impl.KeyboardInputState
import foo.starred.kontext.api.data.Observable.Companion.and
import foo.starred.kontext.api.format.extension.string.stripped
import foo.starred.kontext.api.scheduling.scheduler.extensions.clientTicks
import foo.starred.kontext.api.text.parser.impl.parse
import net.minecraft.client.KeyMapping
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket

@LoadableAthenModule
object WardrobeHelper {

    @Suppress("UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")
    private val wrappedSlots: List<IWardrobeSlot>
        get() = (WardrobeKeybinds as WardrobeKeybindsAccessor).`nebulune$getSlots`() as List<IWardrobeSlot>

    val autoClose by WardrobeKeybinds.config.switch("Auto close after use")
    private val autoEquip = WardrobeKeybinds.config.switch("Auto equip").unique("autoEquip")
    private val _unused by WardrobeKeybinds.config.information("Automatically equips the wardrobe slot without opening the gui. Use at your own risk.")
    private val moveEquip by WardrobeKeybinds.config.switch("Equip while moving")
    private val _unused0 by WardrobeKeybinds.config.information("Equip while moving increases your chances of being banned by a lot.")
    private val resetOpen by WardrobeKeybinds.config.switch("Reset on GUI open", true)
    private val equipDelay by WardrobeKeybinds.config.slider("Click delay", 1, 0, 8, "ticks")
    private val closeDelay by WardrobeKeybinds.config.slider("Close delay", 1, 0, 8, "ticks")
    private val delayVariance by WardrobeKeybinds.config.slider("Max delay variety", 1, 0, 5, "ticks")

    private val display = Ticking {
        val slot = slot0 ?: return@Ticking null
        "Equipping <gray>[<red>${(slot.index - 36) + 1}<gray>]".parse().visualOrderText
    }

    private val render: Boolean
        get() = swapping && slot0 != null

    private val hud by WardrobeKeybinds.config.hud("Display text") {
        val example = "Equipping <gray>[<red>2<gray>]".parse().visualOrderText

        constrain {
            MinecraftFontMeasurer.constrain(example)
        }

        preview {
            MinecraftFontRenderer.extract(graphics, example, 0, 0)
        }

        render {
            if (!render) return@render
            MinecraftFontRenderer.extract(graphics, display.value ?: return@render, 0, 0)
        }
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

    private var slot0: IWardrobeSlot? = null
    private var swapping: Boolean = false
    private var inMenu: Boolean = false
    private var id: Int = -1
    private var wait: Int = 0
    private var start: Long = 0

    init {
        command {
            "wd" / int("slot", 1, 9) {
                if (!WardrobeKeybinds.enabled) return@int "Enable wardrobe keybinds!".mod(MessagePrefixType.ERROR)
                if (!autoEquip.value) return@int "Enable auto equip in wardrobe keybinds!".mod(MessagePrefixType.ERROR)

                val int = int("slot")
                val slot = wrappedSlots.find { it.idx == 35 + int } ?: return@int

                slot0 = slot
                swapping = true
                id = -1
                start = System.currentTimeMillis()

                MinecraftCommands.client("wd")
            }
        }

        on<InputEvent.Keyboard.Press> {
            //~ if >= 26.2 'client.screen' -> 'client.gui.screen()'
            if (client.screen != null) return@on

            val key = KeyboardInputState.vanilla(keyEvent.key)

            if (!moveEquip && swapping) for (a in all) if (a.boundKey() == key) return@on cancel()
            if (swapping) return@on

            val slot = wrappedSlots.find { it.value == key } ?: return@on

            slot0 = slot
            swapping = true
            id = -1
            start = System.currentTimeMillis()

            MinecraftCommands.client("wd")
            cancel()
        }.runWhen(WardrobeKeybinds.observable and autoEquip.state)

        on<PacketEvent.Receive, ClientboundOpenScreenPacket> {
            if (!swapping) return@on
            val stripped = title.stripped()
            if ("Armor Sets" !in stripped && "Wardrobe" !in stripped) return@on
            val player = client.player ?: return@on

            mainThread {
                if (!moveEquip) for (a in all) a.isDown = false
                player.containerMenu = type.create(containerId, player.inventory)
            }

            id = containerId
            wait = equipDelay + (0..delayVariance).random()
            inMenu = true
            it.cancel()
        }.runWhen(WardrobeKeybinds.observable and autoEquip.state)

        on<PacketEvent.Receive, ClientboundContainerClosePacket> {
            reset()
        }.runWhen(WardrobeKeybinds.observable and autoEquip.state)

        on<PacketEvent.Send, ServerboundContainerClosePacket> {
            reset()
        }.runWhen(WardrobeKeybinds.observable and autoEquip.state)

        on<GuiEvent.Open.Container> {
            if (resetOpen && swapping) reset()
        }.runWhen(WardrobeKeybinds.observable and autoEquip.state)

        on<TickEvent.Client.Start> {
            if (!swapping) return@on
            if (System.currentTimeMillis() - start > 2000) {
                if (inMenu || id != -1) close(0)
                return@on reset()
            }
            if (!inMenu) return@on
            if (wait-- > 0) return@on

            val player = client.player ?: return@on
            val menu = player.containerMenu
            val slot = slot0 ?: return@on

            if (menu.containerId != id) return@on

            val mcSlot = menu.slots.getOrNull(slot.index)?.takeIf { !it.item.isEmpty } ?: return@on

            if (!slot.equipped) MinecraftGui.click(id, slot.index)

            close()
            reset()
        }.runWhen(WardrobeKeybinds.observable and autoEquip.state)
    }

    @JvmStatic
    @JvmOverloads
    fun close(i: Int? = null) {
        val player = client.player ?: return
        val delay = (i ?: (closeDelay + (0..delayVariance).random())).coerceAtLeast(0)

        if (delay == 0) {
            player.closeContainer()
        } else {
            Scheduler.schedule(delay.clientTicks) {
                player.closeContainer()
            }
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