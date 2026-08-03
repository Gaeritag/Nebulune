@file:Suppress("ConstPropertyName", "Unused")

package foo.starred.nebulune

import foo.starred.athen.Athen
import foo.starred.athen.annotations.AnnotationLoader
import foo.starred.athen.config.ui.ClickGUI
import foo.starred.athen.handlers.Chronos
import foo.starred.athen.handlers.Typo.modMessage
import foo.starred.snowbird.handlers.time.server
import foo.starred.snowbird.kommand.ICommand
import net.fabricmc.api.ClientModInitializer
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds

object Nebulune : ClientModInitializer, ICommand {
    const val modVersion: String = /*$ mod_version*/ "0.2.1"
    const val modId: String = /*$ mod_id*/ "nebulune"

    override fun onInitializeClient() {
        AnnotationLoader.load("foo.starred.nebulune")
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