package foo.starred.nebulune.modules.impl.render

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.location.SkyBlockIsland
import foo.starred.athen.api.rendering.level.impl.extensions.impl.extractFrameBox
import foo.starred.athen.config.Category
import foo.starred.athen.events.WorldRenderEvent
import foo.starred.athen.modules.Module
import foo.starred.athen.ui.themes.Catppuccin
import foo.starred.athen.utils.render.renderBoundingBox
import foo.starred.athen.utils.render.renderPos
import foo.starred.nebulune.utils.extractTracer
import net.minecraft.client.renderer.entity.state.ShulkerRenderState
import net.minecraft.world.item.DyeColor
import java.awt.Color

@Load
@OnlyIn(islands = [SkyBlockIsland.GALATEA])
object HideonESP : Module(
    "Hideon ESP",
    "ESP for Hideons",
    Category.RENDER
) {
    private val color by config.colorPicker("Color", Color(Catppuccin.Mocha.Mauve.argb, true))
    private val lineWidth by config.slider("Line width", 2f, 1f, 10f)
    private val tracer by config.switch("Show tracer")

    init {
        on<WorldRenderEvent.Entity.Post> {
            val r = renderState as? ShulkerRenderState ?: return@on
            val e = entity ?: return@on
            if (r.color != DyeColor.GREEN) return@on

            extractFrameBox(e.renderBoundingBox, color.rgb, lineWidth, false)
            if (tracer) extractTracer(e.renderPos, color.rgb, lineWidth)
        }
    }
}