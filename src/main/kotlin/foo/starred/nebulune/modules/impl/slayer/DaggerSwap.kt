package foo.starred.nebulune.modules.impl.slayer

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.location.SkyBlockIsland
import foo.starred.athen.config.Category
import foo.starred.athen.ducks.entity.EntityDuck.Companion.attachedNames
import foo.starred.athen.events.PlayerEvent
import foo.starred.athen.events.TickEvent
import foo.starred.athen.modules.Module
import foo.starred.nebulune.mixin.accessors.InventoryAccessor
import foo.starred.nebulune.utils.rightClick
import foo.starred.snowbird.api.client
import foo.starred.snowbird.utils.stripped
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import kotlin.jvm.optionals.getOrNull

@Load
@OnlyIn(islands = [SkyBlockIsland.CRIMSON_ISLE])
object DaggerSwap : Module(
    "Dagger swap",
    "Automatically swaps to the correct dagger for blaze!",
    Category.SLAYER
) {
    private val delay by config.slider("Delay", 1, 0, 10, "ticks")
    private val delayVariance by config.slider("Delay variance", 2, 0, 10, "ticks")

    private var last: Attunements? = null
    private var swap: Attunements? = null
    private var wait: Int = -1

    init {
        on<PlayerEvent.Attack.Entity> {
            for (c in entity.attachedNames) {
                val s = c.string
                if (":" !in s || "♨" !in s) continue

                fn(c)
                break
            }
        }

        on<TickEvent.Client.Start> {
            val swap = swap ?: return@on
            swap(swap)
        }
    }

    private fun fn(component: Component) {
        val n = Attunements.get(component.stripped())?.takeIf { it != last } ?: return

        last = n
        swap = n
        wait = delay + (0..delayVariance).random()
    }

    private fun swap(attr: Attunements) {
        if (wait-- > 0) return
        val inv = client.player?.inventory ?: return
        val acc = inv as InventoryAccessor

        val held = inv.getItem(acc.selectedSlot)
        val id = held.getData(DataTypes.ID)
        if (id in attr.set) {
            if (held.fn() != attr.mode) rightClick()
            swap = null
            return
        }

        for (i in 0..8) {
            val stack = inv.getItem(i)
            val id = stack.getData(DataTypes.ID) ?: continue
            if (id !in attr.set) continue
            if (acc.selectedSlot != i) acc.selectedSlot = i
            return
        }

        swap = null
    }

    private fun ItemStack.fn(): Int {
        return getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("td_attune_mode").getOrNull() ?: -1
    }

    private enum class Attunements(val str: String, val set: Set<String>, val mode: Int) {
        Ashen("ASHEN ♨", setOf("HEARTFIRE_DAGGER", "BURSTFIRE_DAGGER", "FIREDUST_DAGGER"), 0),
        Auric("AURIC ♨", setOf("HEARTFIRE_DAGGER", "BURSTFIRE_DAGGER", "FIREDUST_DAGGER"), 1),
        Spirit("SPIRIT ♨", setOf("HEARTMAW_DAGGER", "BURSTMAW_DAGGER", "MAWDUST_DAGGER"), 2),
        Crystal("CRYSTAL ♨", setOf("HEARTMAW_DAGGER", "BURSTMAW_DAGGER", "MAWDUST_DAGGER"), 3);

        companion object {
            fun get(a: String) = entries.firstOrNull { it.str in a }
        }
    }
}