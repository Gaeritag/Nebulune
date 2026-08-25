package foo.starred.nebulune.modules.impl.render

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.location.SkyBlockIsland
import foo.starred.athen.api.rendering.level.impl.extensions.impl.extractFrameBox
import foo.starred.athen.api.scheduling.Scheduler
import foo.starred.athen.config.Category
import foo.starred.athen.events.LocationEvent
import foo.starred.athen.events.PacketEvent
import foo.starred.athen.events.WorldRenderEvent
import foo.starred.athen.modules.Module
import foo.starred.athen.ui.themes.Catppuccin
import foo.starred.athen.utils.render.renderPos
import foo.starred.nebulune.utils.extractTracer
import foo.starred.snowbird.api.level
import foo.starred.snowbird.handlers.time.client
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Display
import net.minecraft.world.phys.AABB
import tech.thatgravyboat.skyblockapi.utils.extentions.getTexture
import java.awt.Color

@Load
@OnlyIn(islands = [SkyBlockIsland.HUB])
object RatESP : Module(
    "Rat ESP",
    "Shows an ESP for rats in Hub.",
    Category.RENDER
) {
    private const val RAT = "ewogICJ0aW1lc3RhbXAiIDogMTYxODQxOTcwMTc1MywKICAicHJvZmlsZUlkIiA6ICI3MzgyZGRmYmU0ODU0NTVjODI1ZjkwMGY4OGZkMzJmOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJCdUlJZXQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYThhYmI0NzFkYjBhYjc4NzAzMDExOTc5ZGM4YjQwNzk4YTk0MWYzYTRkZWMzZWM2MWNiZWVjMmFmOGNmZmU4IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0="

    private val tracer by config.switch("Tracer")
    private val thickness by config.slider("Thickness", 2, 1, 10)
    private val color by config.colorPicker("Color", Color(Catppuccin.Mocha.Peach.rgba))
    private val entities = mutableSetOf<Entity>()

    init {
        on<PacketEvent.Receive, ClientboundSetEntityDataPacket> {
            Scheduler.schedule(2.client) {
                val entity = level?.getEntity(id) as? Display.ItemDisplay ?: return@schedule
                if (entity in entities) return@schedule
                if (entity.itemStack.getTexture() != RAT) return@schedule

                entities.add(entity)
            }
        }

        on<WorldRenderEvent.Extract> {
            val it = entities.iterator()
            while (it.hasNext()) {
                val e = it.next()
                if (!e.isAlive) {
                    it.remove()
                    continue
                }

                val p = e.renderPos.add(-0.5, 0.0, -0.5)
                extractFrameBox(AABB.unitCubeFromLowerCorner(p), color.rgb, thickness.toFloat(), false)
                if (tracer) extractTracer(p, color.rgb, thickness.toFloat(), false)
            }
        }

        on<LocationEvent.Server.Connect> {
            entities.clear()
        }
    }
}