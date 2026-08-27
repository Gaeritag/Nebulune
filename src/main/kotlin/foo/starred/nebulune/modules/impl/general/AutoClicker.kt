@file:Suppress("ObjectPrivatePropertyName")

package foo.starred.nebulune.modules.impl.general

import com.mojang.serialization.Codec
import foo.starred.athen.annotations.Load
import foo.starred.athen.api.messaging.impl.MessagingAPI.mod
import foo.starred.athen.api.storage.JsonStore
import foo.starred.athen.config.Category
import foo.starred.athen.events.InputEvent
import foo.starred.athen.events.TickEvent
import foo.starred.athen.events.core.runWhen
import foo.starred.athen.mixin.accessors.KeyMappingAccessor
import foo.starred.athen.modules.Module
import foo.starred.nebulune.utils.command
import foo.starred.nebulune.utils.leftClick
import foo.starred.nebulune.utils.rightClick
import foo.starred.snowbird.api.*
import foo.starred.snowbird.handlers.parser.parse
import net.minecraft.client.KeyMapping
import net.minecraft.world.phys.BlockHitResult
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData

@Load
object AutoClicker : Module(
    "Auto clicker",
    "Automatically clicks for you!",
    Category.GENERAL
) {
    private val left by config.group("Left clicker")
    private val `left$enabled` by left.switch("Enable left clicker")
    private val `left$key` by left.keybind("Left key")
    private val `left$cps` by left.slider("Left CPS", 3, 5, 20)

    private val right by config.group("Right clicker")
    private val `right$enabled` by right.switch("Right clicker")
    private val `right$key` by right.keybind("Right key")
    private val `right$cps` by right.slider("Right CPS", 3, 5, 20)

    private val customisation by config.group("Customisation")
    private val jitter by customisation.slider("CPS jitter", 2, 1, 3, "clicks")
    private val breaking = customisation.switch("Allow breaking blocks", true).unique("breaking")
    private val breaker by customisation.switch("Block dungeon breaker", true)
    private val whitelist by customisation.switch("Whitelist mode")

    private val json = JsonStore("features/autoClicker")
    private val set1 = json.mutableSet("left", Codec.STRING)
    private val set2 = json.mutableSet("right", Codec.STRING)

    private var l = 0
    private var r = 0

    init {
        on<TickEvent.Client.Start> {
            //~ if >= 26.2 'client.screen' -> 'client.gui.screen()'
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

            val d = b && `left$enabled` && `left$key`.fn0()
            val e = c && `right$enabled` && `right$key`.fn0()

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

        command {
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