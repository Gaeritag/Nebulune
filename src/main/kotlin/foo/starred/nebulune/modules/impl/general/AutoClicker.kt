@file:Suppress("ObjectPrivatePropertyName")

package foo.starred.nebulune.modules.impl.general

import com.mojang.serialization.Codec
import foo.starred.athen.Athen
import foo.starred.athen.annotations.Load
import foo.starred.athen.api.messaging.impl.MessagingAPI.mod
import foo.starred.athen.api.storage.JsonStore
import foo.starred.athen.config.Category
import foo.starred.athen.events.InputEvent
import foo.starred.athen.events.TickEvent
import foo.starred.athen.events.core.runWhen
import foo.starred.athen.mixin.accessors.KeyMappingAccessor
import foo.starred.athen.modules.Module
import foo.starred.nebulune.utils.leftClick
import foo.starred.nebulune.utils.rightClick
import foo.starred.snowbird.api.*
import foo.starred.snowbird.handlers.parser.parse
import foo.starred.snowbird.kommand.ICommand
import net.minecraft.client.KeyMapping
import net.minecraft.world.phys.BlockHitResult
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData

@Load
object AutoClicker : Module(
    "Auto clicker",
    "Automatically clicks for you!",
    Category.GENERAL
), ICommand {
    private val left by config.switch("Left clicker")
    private val `left$key` by config.keybind("Left key").dependsOn { left }
    private val `left$cps` by config.slider("Left CPS", 3, 5, 20).dependsOn { left }

    private val right by config.switch("Right clicker")
    private val `right$key` by config.keybind("Right key").dependsOn { right }
    private val `right$cps` by config.slider("Right CPS", 3, 5, 20).dependsOn { right }

    private val jitter by config.slider("CPS jitter", 2, 1, 3, "clicks")
    private val breaking = config.switch("Allow breaking blocks", true).custom("breaking")
    private val breaker by config.switch("Block dungeon breaker", true)
    private val whitelist by config.switch("Whitelist mode")

    private val json = JsonStore("features/autoClicker")
    private val set1 = json.mutableSet("left", Codec.STRING)
    private val set2 = json.mutableSet("right", Codec.STRING)

    private var l = 0
    private var r = 0

    init {
        on<TickEvent.Client.Start> {
            if (client.screen != null) return@on
            val p = client.player ?: return@on
            val lv = client.level ?: return@on

            if (p.isUsingItem) return@on
            if (client.gameMode?.isDestroying ?: false) return@on

            val h = fn() ?: return@on
            if (breaker && h == "DUNGEONBREAKER") return@on

            val a = whitelist
            val b = !a || h in set1.value
            val c = !a || h in set2.value
            if (!b && !c) return@on

            val d = b && left && `left$key`.fn0()
            val e = c && right && `right$key`.fn0()

            val h0 = client.hitResult as? BlockHitResult
            if (h0 != null && !lv.getBlockState(h0.blockPos).isAir && d && breaking.value) {
                KeyMapping.set((client.options.keyAttack as KeyMappingAccessor).boundKey, true)
                return@on
            }

            if (d) {
                l += `left$cps`.fn1()
                if (l >= 20) {
                    leftClick()
                    l -= 20
                }
            }

            if (e) {
                r += `right$cps`.fn1()
                if (r >= 20) {
                    rightClick()
                    r -= 20
                }
            }
        }

        on<InputEvent.Keyboard.Release> {
            if (keyEvent.key() != `left$key`) return@on
            KeyMapping.set((client.options.keyAttack as KeyMappingAccessor).boundKey, false)
        }.runWhen(breaking.state)

        command(Athen.modId) {
            "ac" / "add" / "left" {
                val h = fn() ?: return@invoke "Hold an item to whitelist.".mod()
                if (h in set1.value) return@invoke "$h is already in left whitelist!".mod()

                set1.update { add(h) }
                "Added <green>$h<r> to left whitelist!".mod()
            }

            "ac" / "add" / "right" {
                val h = fn() ?: return@invoke "Hold an item to whitelist.".mod()
                if (h in set2.value) return@invoke "$h is already in right whitelist!".mod()

                set2.update { add(h) }
                "Added <green>$h<r> to right whitelist!".mod()
            }

            "ac" / "remove" / "left" {
                val h = fn() ?: return@invoke "Hold an item to whitelist.".mod()
                if (h !in set1.value) return@invoke "$h is not in left whitelist!".mod()

                set1.update { remove(h) }
                "Removed <green>$h<r> from left whitelist!".mod()
            }

            "ac" / "remove" / "right" {
                val h = fn() ?: return@invoke "Hold an item to whitelist.".mod()
                if (h !in set2.value) return@invoke "$h is not in right whitelist!".mod()

                set2.update { remove(h) }
                "Removed <green>$h<r> from right whitelist!".mod()
            }

            "ac" / "clear" / "left" {
                set1.update { clear() }
                "Cleared left whitelist.".mod()
            }

            "ac" / "clear" / "right" {
                set2.update { clear() }
                "Cleared right whitelist.".mod()
            }

            "ac" / "list" {
                val a = ("<gray>" + ("-".repeat())).parse()

                "Autoclicker whitelist:".mod()
                a.lie()

                "Left whitelist:".lie()
                for (s in set1.value) " <dark_gray>- <gray>$s".parse().lie()
                a.lie()

                "Right whitelist:".lie()
                for (s in set2.value) " <dark_gray>- <gray>$s".parse().lie()
                a.lie()

                if (!enabled) "Please turn on the feature \"AutoClicker\"".mod()
            }
        }
    }

    private fun fn(): String? {
        val held = held
        return held?.getData(DataTypes.UUID)?.toString() ?: held?.getData(DataTypes.SKYBLOCK_ID)?.skyblockId ?: held?.hoverName?.string
    }

    private fun Int.fn0(): Boolean {
        if (!bound) return false
        return pressed
    }

    private fun Int.fn1(): Int {
        val a = jitter * 2
        return (this + (-a..a).random()).coerceIn(1, 20)
    }
}