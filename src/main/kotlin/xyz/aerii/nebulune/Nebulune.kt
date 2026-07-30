@file:Suppress("ConstPropertyName", "Unused")

package xyz.aerii.nebulune

import net.fabricmc.api.ClientModInitializer
import xyz.aerii.athen.Athen
import xyz.aerii.athen.config.ui.ClickGUI
import xyz.aerii.athen.handlers.Chronos
import xyz.aerii.athen.handlers.Typo.modMessage
import xyz.aerii.library.handlers.time.server
import xyz.aerii.library.kommand.ICommand
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds

object Nebulune : ClientModInitializer, ICommand {
    const val modVersion: String = /*$ mod_version*/ "0.2.0"
    const val modId: String = /*$ mod_id*/ "nebulune"

    override fun onInitializeClient() {
        Athen.LOGGER.info("Nebulune loaded.")
        command(modId) {
            executes {
                ClickGUI.open()
                "Opening Config GUI...".modMessage()
            }

            "config" {
                ClickGUI.open()
                "Opening Config GUI...".modMessage()
            }
        }
    }

    @JvmStatic
    fun afterTimed(ms: Int, action: () -> Unit) {
        val count = AtomicInteger(0)
        val check = { if (count.incrementAndGet() == 2) action() }

        Chronos.schedule(ms.milliseconds) { check() }

        if (ms < 15) return check()
        Chronos.schedule(((ms / 50).coerceAtLeast(1)).server) { check() }
    }
}