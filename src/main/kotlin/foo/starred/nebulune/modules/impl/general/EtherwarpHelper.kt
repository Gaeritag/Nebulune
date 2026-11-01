package foo.starred.nebulune.modules.impl.general

import foo.starred.athen.annotations.impl.LoadableAthenModule
import foo.starred.athen.annotations.impl.AthenModuleScope
import foo.starred.athen.api.scheduling.Scheduler
import foo.starred.athen.config.dsl.impl.category.ConfigCategory
import foo.starred.athen.events.InputEvent
import foo.starred.athen.modules.Module
import foo.starred.kbus.extensions.runWhen
import foo.starred.nebulune.utils.rightClick
import foo.starred.kontext.abstraction.client.extension.boundKey
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.api.scheduling.scheduler.extensions.clientTicks
import foo.starred.kontext.api.scheduling.scheduler.extensions.start
import net.minecraft.client.KeyMapping
import net.minecraft.core.component.DataComponents
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
//? if >= 26.3 {
/*import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.component.SwingAnimation
*///?}

@LoadableAthenModule
@AthenModuleScope(skyblock = true)
object EtherwarpHelper : Module(
    "Etherwarp helper",
    "Helper features for Etherwarp.",
    ConfigCategory.GENERAL
) {
    private val lcew = config.switch("Left click warp").unique("lcew")
    private val shift by config.switch("Shift automatically")

    private val ints = intArrayOf(2, 3, 4)

    fun ItemStack.etherwarp(): Boolean {
        return get(DataComponents.CUSTOM_DATA)?.copyTag()?.getBoolean("ethermerge")?.orElse(false) == true || getData(DataTypes.ID) == "ETHERWARP_CONDUIT"
    }

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
                KeyMapping.set(client.options.keyShift.boundKey(), true)
                Scheduler.schedule(ints.random().clientTicks.start) {
                    action()

                    Scheduler.schedule(1.clientTicks.start) {
KeyMapping.set(client.options.keyShift.boundKey(), false)
                    }
                }

                return@on cancel()
            }

            cancel()
            action()
        }.runWhen(lcew.state)
    }

    private fun action() {
        rightClick()

        //? if >= 26.3 {
        /*val p = client.player ?: return
        if (p.isSwinging) return
        val swingState = p.swingState
        swingState.startIfAble(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, SwingAnimation.DEFAULT.duration)
        *///?} else {
        with(client.player ?: return) {
            if (swinging && swingTime >= 0) return

            swingingArm = InteractionHand.MAIN_HAND
            swingTime = -1
            swinging = true
        }
        //?}
    }
}
