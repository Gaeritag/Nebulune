package foo.starred.nebulune.modules.impl.general

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.scheduling.Scheduler
import foo.starred.athen.config.Category
import foo.starred.athen.events.InputEvent
import foo.starred.athen.events.core.runWhen
import foo.starred.athen.mixin.accessors.KeyMappingAccessor
import foo.starred.athen.modules.Module
import foo.starred.athen.utils.etherwarp
import foo.starred.nebulune.utils.rightClick
import foo.starred.snowbird.api.client
import foo.starred.snowbird.api.scheduling.scheduler.extensions.clientTicks
import foo.starred.snowbird.api.scheduling.scheduler.extensions.start
import net.minecraft.client.KeyMapping
import net.minecraft.world.InteractionHand

@Load
@OnlyIn(skyblock = true)
object EtherwarpHelper : Module(
    "Etherwarp helper",
    "Helper features for Etherwarp.",
    Category.GENERAL
) {
    private val lcew = config.switch("Left click warp").unique("lcew")
    private val shift by config.switch("Shift automatically")

    private val ints = intArrayOf(2, 3, 4)

    init {
        on<InputEvent.Mouse.Press> {
            //~ if >= 26.2 'client.screen' -> 'client.gui.screen()'
            if (client.screen != null) return@on
            if (buttonInfo.button != 0) return@on

            val p = client.player ?: return@on
            if (!p.mainHandItem.etherwarp()) return@on

            val a = p.isCrouching
            if (!a && !shift) return@on

            if (!a) {
                KeyMapping.set((client.options.keyShift as KeyMappingAccessor).boundKey, true)
                Scheduler.schedule(ints.random().clientTicks.start) {
                    action()

                    Scheduler.schedule(1.clientTicks.start) { KeyMapping.set((client.options.keyShift as KeyMappingAccessor).boundKey, false) }
                }

                return@on cancel()
            }

            cancel()
            action()
        }.runWhen(lcew.state)
    }

    private fun action() {
        rightClick()
        with(client.player ?: return) {
            if (swinging && swingTime >= 0) return

            swingingArm = InteractionHand.MAIN_HAND
            swingTime = -1
            swinging = true
        }
    }
}