package foo.starred.nebulune.modules.impl.render

import foo.starred.athen.annotations.impl.AthenModuleScope
import foo.starred.athen.annotations.impl.LoadableAthenModule
import foo.starred.athen.api.location.island.impl.PresetSkyBlockIsland
import foo.starred.athen.config.dsl.impl.category.ConfigCategory
import foo.starred.athen.config.theme.impl.catppuccin.MochaColorScheme
import foo.starred.athen.events.WorldRenderEvent
import foo.starred.athen.modules.Module
import foo.starred.kontext.api.entity.extension.renderAABB
import foo.starred.kontext.api.entity.extension.renderVec3
import foo.starred.nebulune.utils.extractTracer
import foo.starred.parallax.api.primitives.ParallaxBox
import net.minecraft.client.renderer.entity.state.ShulkerRenderState
import net.minecraft.world.item.DyeColor

@LoadableAthenModule
@AthenModuleScope(islands = [PresetSkyBlockIsland.GALATEA])
object HideonESP : Module(
    "Hideon ESP",
    "ESP for Hideons",
    ConfigCategory.RENDER
) {
    private val color by config.colorPicker("Color", MochaColorScheme.Peach.argb)
    private val lineWidth by config.slider("Line width", 2f, 1f, 10f)
    private val tracer by config.switch("Show tracer")

    init {
        on<WorldRenderEvent.Entity> {
            val r = renderState as? ShulkerRenderState ?: return@on
            val e = entity ?: return@on
            if (r.color != DyeColor.GREEN) return@on

            ParallaxBox.frame(e.renderAABB(), color, lineWidth, false)
            if (tracer) extractTracer(e.renderVec3(), color, lineWidth)
        }
    }
}