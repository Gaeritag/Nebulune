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
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.AABB
import tech.thatgravyboat.skyblockapi.utils.extentions.getTexture
import java.awt.Color

@Load
@OnlyIn(islands = [SkyBlockIsland.GARDEN])
object PestESP : Module(
    "Pest ESP",
    "ESPs for Pests!",
    Category.RENDER
) {
    private val tracer by config.switch("Tracer")
    private val thickness by config.slider("Thickness", 2, 1, 10)
    private val color by config.colorPicker("ESP color", Color(Catppuccin.Mocha.Peach.rgba))
    private val depthTest by config.switch("Depth test")
    private val entities = mutableSetOf<Entity>()

    init {
        on<PacketEvent.Receive, ClientboundSetEntityDataPacket> {
            Scheduler.schedule(2.client) {
                val entity = level?.getEntity(id) as? ArmorStand ?: return@schedule
                if (!entity.hasItemInSlot(EquipmentSlot.HEAD)) return@schedule
                if (entity in entities) return@schedule

                val head = entity.getItemBySlot(EquipmentSlot.HEAD).getTexture() ?: return@schedule
                if (head !in PEST_HEADS) return@schedule

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

                val p = e.renderPos.add(-0.5, 1.0, -0.5)
                extractFrameBox(AABB.unitCubeFromLowerCorner(p), color.rgb, thickness.toFloat(), depthTest)
                if (tracer) extractTracer(p, color.rgb, thickness.toFloat(), depthTest)
            }
        }

        on<LocationEvent.Server.Connect> {
            entities.clear()
        }
    }

    /*
     * Original work by [Skyblocker](https://github.com/SkyblockerMod/Skyblocker) and contributors (LGPL-3.0 License).
     *
     * Modifications:
     *   Copyright (c) 2025 skies-starred
     *   Licensed under the BSD 3-Clause License.
     *
     * The original LGPL-3.0 license applies to the portions derived from Skyblocker.
     */
    private val PEST_HEADS = setOf(
        "ewogICJ0aW1lc3RhbXAiIDogMTcyMzE3OTc4OTkzNCwKICAicHJvZmlsZUlkIiA6ICJlMjc5NjliODYyNWY0NDg1YjkyNmM5NTBhMDljMWMwMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJLRVZJTktFTE9LRSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83MGExZTgzNmJmMTk2OGIyZWFhNDgzNzIyN2ExOTIwNGYxNzI5NWQ4NzBlZTllNzU0YmQ2YjZkNjBkZGJlZDNjIgogICAgfQogIH0KfQ==",
        "ewogICJ0aW1lc3RhbXAiIDogMTcyMzE3OTgxMTI2NCwKICAicHJvZmlsZUlkIiA6ICJjZjc4YzFkZjE3ZTI0Y2Q5YTIxYmU4NWQ0NDk5ZWE4ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNYXR0c0FybW9yU3RhbmRzIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2EyNGM2OWY5NmNlNTU2MjIxZTE5NWM4ZWYyYmZhZDcxZWJmN2Y5NWY1YWU5MTRhNDg0YThkMGVjMjE2NzI2NzQiCiAgICB9CiAgfQp9",
        "ewogICJ0aW1lc3RhbXAiIDogMTc2MDQ1MDQxODQzNywKICAicHJvZmlsZUlkIiA6ICIwNjY5Y2E1MGYyZWU0NTQxODhlYWQ3YTM3NTkzNDRlMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDcjR6eWNsb3duVFYiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjU0YWZmNGMwYjJkY2UzYTY3MjM0OWNjMGVlOWU2ZjNhOWRlZWJlNGIzNTU2ZTg0NjExZWNhMjUwYTc4MjFiZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        "ewogICJ0aW1lc3RhbXAiIDogMTY5NzQ3MDQ1OTc0NywKICAicHJvZmlsZUlkIiA6ICIyNTBlNzc5MjZkNDM0ZDIyYWM2MTQ4N2EyY2M3YzAwNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJMdW5hMTIxMDUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjQwM2JhNDAyN2EzMzNkOGQyZmQzMmFiNTlkMWNmZGJhYTdkOTA4ZDgwZDIzODFkYjJhNjljYmU2NTQ1MGFkOCIKICAgIH0KICB9Cn0=",
        "ewogICJ0aW1lc3RhbXAiIDogMTY5NzQ3MDQ3ODAzMCwKICAicHJvZmlsZUlkIiA6ICI0NmY3N2NjNmQ2MjU0NjEzYjc2NmYyZDRmMDM2MzZhNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaXNzV29sZiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9mZDQwYWE1MDkwNTIzNWI2MjhlNzM3OWViMzFmYTQ1Y2Q0MWI1MDNmMDk3MjFkYjNjNDM3ZmNlZTM5MjA3ZGZjIgogICAgfQogIH0KfQ==",
        "ewogICJ0aW1lc3RhbXAiIDogMTcyNzkwNDc5NzQ1OSwKICAicHJvZmlsZUlkIiA6ICI0MmIwOTMyZDUwMWI0MWQ1YTM4YjEwOTcxYTYwYmYxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJBaXJib2x0MDc4IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2YzNzllMDkyNTI4MTczMTRiZDBiNjk0ZjdkNTNiNDhhZjJjN2ZhODQ5OTEwOTgwMmE0MWJiMjk0ZDJmOTNlM2UiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
        "ewogICJ0aW1lc3RhbXAiIDogMTc2MDQ1MDQyMjEzNiwKICAicHJvZmlsZUlkIiA6ICIzNDY4Y2VjMWFlOTY0YWRmYWQyNjEzMGEwZGQ0NjRkYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJzdXJlZWxta18iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNlNzllOTBhZGYzNDcxOGYzMTNlYzI0ZDZjNjEzNWI2OWIzNzg4YzYxODQ5ODQ0NmNjYzgzY2E2NDBjMGIxNCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        "ewogICJ0aW1lc3RhbXAiIDogMTc2MDQ1MDQyMzg4OSwKICAicHJvZmlsZUlkIiA6ICIyY2Y2MzExZjUyMTM0NTE2YTEyNTY3NWUwMzk3NmU2MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJmaWdodHN0b2NrIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzNlNTI3ODJkN2YyYWFlZThhZjViYTI5MjhmZWM3ODg1ZTk0ODc5MzM0YzIyOTZiYzllN2UyZGJjNTQxOGU1OGYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
        "ewogICJ0aW1lc3RhbXAiIDogMTY5Njk0NTA2MzI4MSwKICAicHJvZmlsZUlkIiA6ICJjN2FmMWNkNjNiNTE0Y2YzOGY4NWQ2ZDUxNzhjYThlNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJtb25zdGVyZ2FtZXIzMTUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ5MGU3Nzc4MjZhNTI0NjEzNjhlMjZkMWIyZTE5YmZhMWJhNTgyZDYwMjQ4M2U1NDVmNDEyNGQwZjczMTg0MiIKICAgIH0KICB9Cn0=",
        "ewogICJ0aW1lc3RhbXAiIDogMTY5NzU1NzA3NzAzNywKICAicHJvZmlsZUlkIiA6ICI0YjJlMGM1ODliZjU0ZTk1OWM1ZmJlMzg5MjQ1MzQzZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJfTmVvdHJvbl8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGIyNGE0ODJhMzJkYjFlYTc4ZmI5ODA2MGIwYzJmYTRhMzczY2JkMThhNjhlZGRkZWI3NDE5NDU1YTU5Y2RhOSIKICAgIH0KICB9Cn0=",
        "ewogICJ0aW1lc3RhbXAiIDogMTY5Njg3MDQxOTcyNSwKICAicHJvZmlsZUlkIiA6ICJkYjYzNWE3MWI4N2U0MzQ5YThhYTgwOTMwOWFhODA3NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJFbmdlbHMxNzQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmU2YmFmNjQzMWE5ZGFhMmNhNjA0ZDVhM2MyNmU5YTc2MWQ1OTUyZjA4MTcxNzRhNGZlMGI3NjQ2MTZlMjFmZiIKICAgIH0KICB9Cn0=",
        "ewogICJ0aW1lc3RhbXAiIDogMTY5Njk0NTAyOTQ2MSwKICAicHJvZmlsZUlkIiA6ICI3NTE0NDQ4MTkxZTY0NTQ2OGM5NzM5YTZlMzk1N2JlYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGFua3NNb2phbmciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTJhOWZlMDViYzY2M2VmY2QxMmU1NmEzY2NjNWVjMDM1YmY1NzdiNzg3MDg1NDhiNmY0ZmZjZjFkMzBlY2NmZSIKICAgIH0KICB9Cn0=",
        "ewogICJ0aW1lc3RhbXAiIDogMTY5Njg3MDQwNTk1NCwKICAicHJvZmlsZUlkIiA6ICJiMTUyZDlhZTE1MTM0OWNmOWM2NmI0Y2RjMTA5NTZjOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaXNxdW90aCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82NTQ4NWM0YjM0ZTViNTQ3MGJlOTRkZTEwMGU2MWY3ODE2ZjgxYmM1YTExZGZkZjBlY2NmODkwMTcyZGE1ZDBhIgogICAgfQogIH0KfQ==",
        "ewogICJ0aW1lc3RhbXAiIDogMTc2MDQ1MDQxOTYxMiwKICAicHJvZmlsZUlkIiA6ICI0OWIzODUyNDdhMWY0NTM3YjBmN2MwZTFmMTVjMTc2NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJiY2QyMDMzYzYzZWM0YmY4IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFlMDRiYjYzNjdjYWE0ZTg4ZjVmZDBlZTgwZjA3NDVkMTM3YTYwNjAyMjNkYmJjNDJhMTY0NzFmZGY2NGJiODMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
        "ewogICJ0aW1lc3RhbXAiIDogMTYxODQxOTcwMTc1MywKICAicHJvZmlsZUlkIiA6ICI3MzgyZGRmYmU0ODU0NTVjODI1ZjkwMGY4OGZkMzJmOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJCdUlJZXQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYThhYmI0NzFkYjBhYjc4NzAzMDExOTc5ZGM4YjQwNzk4YTk0MWYzYTRkZWMzZWM2MWNiZWVjMmFmOGNmZmU4IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
        "ewogICJ0aW1lc3RhbXAiIDogMTY5NzQ3MDQ0MzA4MiwKICAicHJvZmlsZUlkIiA6ICJkOGNkMTNjZGRmNGU0Y2IzODJmYWZiYWIwOGIyNzQ4OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJaYWNoeVphY2giLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2E3OWQwZmQ2NzdiNTQ1MzA5NjExMTdlZjg0YWRjMjA2ZTJjYzUwNDVjMTM0NGQ2MWQ3NzZiZjhhYzJmZTFiYSIKICAgIH0KICB9Cn0="
    )
}
