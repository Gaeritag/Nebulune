@file:Suppress("ConstPropertyName", "Unused")

package foo.starred.nebulune

import foo.starred.athen.Athen
import foo.starred.athen.annotations.AnnotationLoader
import foo.starred.athen.api.scheduling.Scheduler
import foo.starred.snowbird.api.scheduling.scheduler.extensions.serverTicks
import net.fabricmc.api.ClientModInitializer
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds

object Nebulune : ClientModInitializer {
    const val modVersion: String = /*$ mod_version*/ "0.3.0"
    const val modId: String = /*$ mod_id*/ "nebulune"

    override fun onInitializeClient() {
        AnnotationLoader.load("foo.starred.nebulune")
        Athen.LOGGER.info("Nebulune loaded.")
    }

    @JvmStatic
    fun afterTimed(ms: Int, action: () -> Unit) {
        val count = AtomicInteger(0)
        val check = { if (count.incrementAndGet() == 2) action() }

        Scheduler.schedule(ms.milliseconds) { check() }

        if (ms < 15) return check()
        Scheduler.schedule(((ms / 50).coerceAtLeast(1)).serverTicks) { check() }
    }
}