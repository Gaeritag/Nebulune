@file:Suppress("ObjectPrivatePropertyName", "Unused")

package foo.starred.nebulune.modules.impl.dungeons

import com.mojang.serialization.Codec
import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.location.SkyBlockIsland
import foo.starred.athen.config.Category
import foo.starred.athen.events.InputEvent
import foo.starred.athen.handlers.Chronos
import foo.starred.athen.handlers.Scribble
import foo.starred.athen.handlers.Typo.modMessage
import foo.starred.athen.modules.Module
import foo.starred.nebulune.Nebulune
import foo.starred.nebulune.mixin.accessors.InventoryAccessor
import foo.starred.nebulune.utils.leftClick
import foo.starred.snowbird.api.client
import foo.starred.snowbird.api.lie
import foo.starred.snowbird.handlers.parser.parse
import foo.starred.snowbird.handlers.time.client
import foo.starred.snowbird.handlers.time.start
import foo.starred.snowbird.kommand.ICommand
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.phys.BlockHitResult
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId

@Load
@OnlyIn(islands = [SkyBlockIsland.THE_CATACOMBS])
object AutoSuperboom : Module(
    "Auto superboom",
    "Automatically swaps to and uses the superboom if clicking on a breakable wall.",
    Category.DUNGEONS
), ICommand {
    private val _unused by config.textParagraph("Use the command <red>\"/nebulune superboom [add|remove]\"<r> while looking at a block to add/remove it to the breakable blocks list!")

    private val minDelay by config.slider("Minimum delay", 1, 1, 5, "ticks")
    private val maxDelay by config.slider("Maximum delay", 3, 1, 5, "ticks")

    private val swapBack by config.switch("Swap back")
    private val `swapBack$minDelay` by config.slider("Minimum delay", 1, 1, 5, "ticks").dependsOn { swapBack }
    private val `swapBack$maxDelay` by config.slider("Maximum delay", 3, 1, 5, "ticks").dependsOn { swapBack }
    private val `swapBack$type` by config.dropdown("Swap to", listOf("Original slot", "Custom slot")).dependsOn { swapBack }
    private val `swapBack$custom` by config.slider("Custom slot number", 1, 1, 9).dependsOn { swapBack && `swapBack$type` == 1 }

    private val scribble = Scribble("features/autoSuperboom")
    private val breakable = scribble.mutableSet("breakable", Codec.STRING, mutableSetOf("minecraft:cracked_stone_bricks", "minecraft:barrier"))

    private val set = setOf("SUPERBOOM_TNT", "INFINITE_SUPERBOOM_TNT")

    init {
        command(Nebulune.modId) {
            "superboom" / "add" {
                val h = client.hitResult as? BlockHitResult ?: return@invoke "Not looking at a block!".modMessage()
                val b = client.level?.getBlockState(h.blockPos)?.block ?: return@invoke

                val id = BuiltInRegistries.BLOCK.getKey(b)

                if (id.toString() in breakable.value) return@invoke "Block already in breakable list!".modMessage()
                breakable.update { add(id.toString()) }

                "Added \"${id.path}\" to the breakable block list!".modMessage()
            }

            "superboom" / "add" / string("block") {
                val it = "minecraft:${string("block")}"
                if (Identifier.tryParse(it) == null) return@string "Invalid block id, or format! Try the command \"/nebulune superboom add\" while looking at the block.".modMessage()
                if (it in breakable.value) return@string "Block already in breakable list!".modMessage()

                breakable.update { add(it) }
                "Added \"${it.substringAfter(":")}\" to the breakable block list!".modMessage()
            }

            "superboom" / "remove" {
                val h = client.hitResult as? BlockHitResult ?: return@invoke "Not looking at a block!".modMessage()
                val b = client.level?.getBlockState(h.blockPos)?.block ?: return@invoke

                val id = BuiltInRegistries.BLOCK.getKey(b)

                if (id.toString() !in breakable.value) return@invoke "Block not in breakable list!".modMessage()
                breakable.update { remove(id.toString()) }

                "Removed \"${id.path}\" from the breakable block list!".modMessage()
            }

            "superboom" / "remove" / string("block") {
                val it = "minecraft:${string("block")}"
                if (Identifier.tryParse(it) == null) return@string "Invalid block id, or format! Try the command \"/nebulune superboom add\" while looking at the block.".modMessage()
                if (it !in breakable.value) return@string "Block not in breakable list!".modMessage()

                breakable.update { remove(it) }
                "Removed \"${it.substringAfter(":")}\" from the breakable block list!".modMessage()
            }

            "supeboom" / "list" {
                "Breakable block list:".modMessage()
                for (a in breakable.value) {
                    val b = a.substringBefore(":")
                    val c = a.substringAfter(":")

                    " <dark_gray>- <gray>$b:<r>$c".parse().lie()
                }
            }
        }

        on<InputEvent.Mouse.Press> {
            if (client.screen != null) return@on
            val p = client.player ?: return@on
            val h = client.hitResult as? BlockHitResult ?: return@on

            val block = client.level?.getBlockState(h.blockPos) ?: return@on
            if (BuiltInRegistries.BLOCK.getKey(block.block).toString() !in breakable.value) return@on

            val acc = p.inventory as InventoryAccessor
            val s = acc.selectedSlot
            val t = fn()?.takeIf { it != s } ?: return@on

            cancel()

            Chronos.schedule((minDelay..maxDelay.coerceAtLeast(minDelay)).random().client.start) {
                acc.selectedSlot = t

                Chronos.schedule(1.client.start) {
                    leftClick()

                    if (!swapBack) return@schedule
                    val b = (`swapBack$minDelay`..`swapBack$maxDelay`.coerceAtLeast(`swapBack$minDelay`)).random()

                    Chronos.schedule(b.client.start) {
                        acc.selectedSlot = if (`swapBack$type` == 0) s else (`swapBack$custom`.coerceIn(1, 9) - 1)
                    }
                }
            }
        }
    }

    private fun fn(): Int? {
        val player = client.player ?: return null
        for (i in 0..8) if (player.inventory.getItem(i).getSkyBlockId()?.skyblockId in set) return i
        return null
    }
}