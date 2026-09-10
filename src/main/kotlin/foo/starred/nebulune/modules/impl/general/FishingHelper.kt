@file:Suppress("ObjectPrivatePropertyName", "Unused")

package foo.starred.nebulune.modules.impl.general

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.scheduling.Scheduler
import foo.starred.athen.config.Category
import foo.starred.athen.events.EntityEvent
import foo.starred.athen.modules.Module
import foo.starred.nebulune.utils.rightClick
import foo.starred.snowbird.api.client
import foo.starred.snowbird.api.held
import foo.starred.snowbird.api.player
import foo.starred.snowbird.api.scheduling.scheduler.extensions.clientTicks
import foo.starred.snowbird.api.scheduling.scheduler.extensions.start
import foo.starred.snowbird.utils.stripped
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.item.Items

@Load
@OnlyIn(skyblock = true)
object FishingHelper : Module(
    "Fishing helper",
    "Helper features for fishing.",
    Category.GENERAL
) {
    private val autoPull by config.switch("Auto pull", true)
    private val `delay$pull` by config.slider("Delay", 1, 0, 5, "ticks")
    private val `variance$pull` by config.slider("Delay variance", 0, 0, 3, "ticks")

    private val recast by config.switch("Auto recast")
    private val `_recast$check` by config.information("Recast check checks if the fishing rod is already being used, and uses it if not.")
    private val `recast$check` by config.switch("Recast check")
    private val `delay$recast` by config.slider("Recast delay", 1, 0, 10, "ticks")
    private val `variance$recast` by config.slider("Delay variance", 0, 0, 5, "ticks")

    private var bobber: FishingHook? = null

    init {
        on<EntityEvent.Load> {
            val e = entity as? FishingHook ?: return@on
            if (e.owner != player) return@on
            bobber = e
        }

        on<EntityEvent.Unload> {
            val e = entity as? FishingHook ?: return@on
            if (e != bobber) return@on
            bobber = null
        }

        on<EntityEvent.Update.Named> {
            if (!enabled || !autoPull) return@on

            val hook = bobber ?: return@on
            if (component.stripped() != "!!!") return@on
            if (entity.distanceTo(hook) > 2f) return@on

            val pullDelay = (`delay$pull` + if (`variance$pull` > 0) (0..`variance$pull`).random() else 0).coerceAtLeast(0)

            Scheduler.schedule(pullDelay.clientTicks.start) {
                rightClick()

                if (!recast) return@schedule

                val recastDelay = 2 + `delay$recast` + if (`variance$recast` > 0) (0..`variance$recast`).random() else 0
                Scheduler.schedule(recastDelay.clientTicks.start) {
                    rightClick()
                }
            }
        }

        Scheduler.repeat((15 * 20).clientTicks.start) {
            if (!enabled || !recast || !`recast$check`) return@repeat
            if (client.player == null) return@repeat
            if (bobber?.isAlive == true) return@repeat
            if (held?.item != Items.FISHING_ROD) return@repeat

            rightClick()
        }
    }
}