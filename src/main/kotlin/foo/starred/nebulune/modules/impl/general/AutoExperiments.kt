@file:Suppress("Unused")

package foo.starred.nebulune.modules.impl.general

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.location.SkyBlockIsland
import foo.starred.athen.config.Category
import foo.starred.athen.events.GuiEvent
import foo.starred.athen.events.PacketEvent
import foo.starred.athen.events.TickEvent
import foo.starred.athen.modules.Module
import foo.starred.athen.utils.glint
import foo.starred.athen.utils.guiClick
import foo.starred.snowbird.api.client
import foo.starred.snowbird.utils.stripped
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.Items

@Load
@OnlyIn(islands = [SkyBlockIsland.PRIVATE_ISLAND])
object AutoExperiments : Module(
    "Auto experiments",
    "Automatically does experiments for you!",
    Category.GENERAL
) {
    private val _unused0 by config.textParagraph("Please disable SkyHanni's experiment solver if you have it enabled!")
    private val minDelay by config.slider("Min click delay", 200, 100, 1000, "ms")
    private val maxDelay by config.slider("Max click delay", 250, 100, 1000, "ms")
    private val autoClose by config.switch("Auto close")
    private val serums by config.slider("Serums applied", 0, 0, 3)
    private val max by config.switch("Get max")

    private var current: ExperimentType? = null
    private var click: Long = 0

    init {
        on<GuiEvent.Open.Container> {
            val title = screen.title.string

            current = when {
                title.startsWith("Chronomatron (") -> ExperimentType.Chronomatron
                title.startsWith("Ultrasequencer (") -> ExperimentType.Ultrasequencer
                else -> null
            }

            current?.reset()
        }

        on<GuiEvent.Input.Mouse.Press> {
            if (current == null) return@on
            cancel()
        }

        on<PacketEvent.Receive, ClientboundContainerSetSlotPacket> {
            val screen = client.screen as? AbstractContainerScreen<*> ?: return@on
            current?.fn(screen)
        }

        on<TickEvent.Client.Start> {
            val a = current ?: return@on
            val s = client.screen as? AbstractContainerScreen<*> ?: return@on

            val n = System.currentTimeMillis()
            if (n - click < delay()) return@on

            val b = a.next
            if (b != null) {
                guiClick(s.menu.containerId, b, clickType = ClickType.CLONE)
                click = n
            }

            if (!a.close) return@on
            client.player?.closeContainer()
            current = null
        }
    }

    private fun delay(): Long =
        (minDelay..maxDelay.coerceAtLeast(minDelay)).random().toLong()

    private enum class ExperimentType {
        Chronomatron {
            private val order = mutableListOf<Int>()
            private var last = -1
            private var ready = false
            private var clicks = 0
            private var bool = false

            override fun reset() {
                order.clear()
                last = -1
                ready = false
                clicks = 0
                bool = false
            }

            override val next: Int?
                get() = if (ready && clicks < order.size) order[clicks++] else null

            override val close: Boolean
                get() = autoClose && bool && clicks >= order.size

            override fun fn(screen: AbstractContainerScreen<*>) {
                val slots = screen.menu.slots
                val center = slots[49].item

                if (last != -1 && center.item == Items.GLOWSTONE && !slots[last].item.glint()) {
                    bool = order.size > if (max) 15 else 11 - serums
                    ready = false
                    return
                }

                if (ready || center.item != Items.CLOCK) return

                val slot = slots.firstOrNull { it.index in 10..43 && it.item.glint() } ?: return

                order.add(slot.index)
                last = slot.index
                ready = true
                clicks = 0
            }
        },
        Ultrasequencer {
            private val regex = Regex("\\d+")
            private val order = HashMap<Int, Int>()
            private var ready = false
            private var clicks = 0

            override fun reset() {
                order.clear()
                ready = false
                clicks = 0
            }

            override val next: Int?
                get() = if (!ready) order[clicks++] else null

            override val close: Boolean
                get() = autoClose && order.size > if (max) 20 else 9 - serums

            override fun fn(screen: AbstractContainerScreen<*>) {
                val slots = screen.menu.slots
                val center = slots[49].item

                if (center.item == Items.CLOCK) return ::ready.set(false)
                if (ready || center.item != Items.GLOWSTONE) return

                order.clear()
                for (slot in slots) if (slot.index in 9..44 && slot.item.hoverName.stripped().matches(regex)) order[slot.item.count - 1] = slot.index

                ready = true
                clicks = 0
            }
        };

        abstract val next: Int?
        abstract val close: Boolean
        abstract fun fn(screen: AbstractContainerScreen<*>)
        abstract fun reset()
    }
}