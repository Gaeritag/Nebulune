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
import foo.starred.snowbird.handlers.time.client
import foo.starred.snowbird.handlers.time.start
import foo.starred.snowbird.utils.stripped
import net.minecraft.world.entity.Entity
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

    init {
        on<EntityEvent.Update.Named> {
            val entity = client.player?.fishing as? Entity ?: return@on
            if (component.stripped() != "!!!") return@on
            if (entity.distanceTo(entity) > 2f) return@on

            val a = (`delay$pull` + if (`variance$pull` > 0) (0..`variance$pull`).random() else 0).coerceAtLeast(0)

            Scheduler.schedule(a.client.start) {
                rightClick()

                if (!recast) return@schedule

                val b = 2 + `delay$recast` + if (`variance$recast` > 0) (0..`variance$recast`).random() else 0
                Scheduler.schedule(b.client.start) {
                    rightClick()
                }
            }
        }

        Scheduler.repeat((15 * 20).client.start) {
            if (!enabled) return@repeat
            if (!recast) return@repeat
            if (!`recast$check`) return@repeat
            val p = client.player ?: return@repeat

            val b = p.fishing
            if (b != null && b.isAlive) return@repeat
            if (held?.item != Items.FISHING_ROD) return@repeat

            rightClick()
        }
    }
}