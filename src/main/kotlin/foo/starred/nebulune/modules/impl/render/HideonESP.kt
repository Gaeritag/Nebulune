package foo.starred.nebulune.modules.impl.render

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.location.island.impl.PresetSkyBlockIsland
import foo.starred.athen.config.dsl.impl.category.ConfigCategory
import foo.starred.athen.config.theme.impl.catppuccin.MochaColorScheme
import foo.starred.athen.events.WorldRenderEvent
import foo.starred.athen.modules.Module
import foo.starred.athen.utils.render.renderBoundingBox
import foo.starred.athen.utils.render.renderPos
import foo.starred.nebulune.utils.extractTracer
import foo.starred.parallax.api.primitives.ParallaxBox
import net.minecraft.client.renderer.entity.state.ShulkerRenderState
import net.minecraft.world.item.DyeColor

@Load
@OnlyIn(islands = [PresetSkyBlockIsland.GALATEA])
object HideonESP : Module(
    "Hideon ESP",
    "ESP for Hideons",
    ConfigCategory.RENDER
) {
    private val color by config.colorPicker("Color", MochaColorScheme.Mauve.argb)
    private val lineWidth by config.slider("Line width", 2f, 1f, 10f)
    private val tracer by config.switch("Show tracer")

    init {
        on<WorldRenderEvent.Entity> {
            val r = renderState as? ShulkerRenderState ?: return@on
            val e = entity ?: return@on
            if (r.color != DyeColor.GREEN) return@on

            ParallaxBox.frame(e.renderBoundingBox, color, lineWidth, false)
            if (tracer) extractTracer(e.renderPos, color, lineWidth)
        }
    }
}