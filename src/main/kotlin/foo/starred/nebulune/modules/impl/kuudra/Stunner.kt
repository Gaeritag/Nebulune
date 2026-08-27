@file:Suppress("ObjectPrivatePropertyName")

package foo.starred.nebulune.modules.impl.kuudra

import foo.starred.athen.annotations.Load
import foo.starred.athen.api.scheduling.Scheduler
import foo.starred.athen.modules.impl.kuudra.StunHelper
import foo.starred.snowbird.api.client
import foo.starred.snowbird.api.scheduling.scheduler.extensions.clientTicks

@Load
object Stunner {
    private val autoClose by StunHelper.config.switch("Auto close GUI")
    private val `autoClose$delay` by StunHelper.config.slider("Close delay", 1, 0, 5, "ticks")

    @JvmStatic
    fun fn() {
        if (!autoClose) return
        val player = client.player ?: return
        val menu = player.containerMenu ?: return

        Scheduler.schedule(`autoClose$delay`.clientTicks) {
            if (menu == player.containerMenu) player.closeContainer()
        }
    }
}