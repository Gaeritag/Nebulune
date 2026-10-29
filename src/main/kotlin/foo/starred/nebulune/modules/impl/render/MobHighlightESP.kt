package foo.starred.nebulune.modules.impl.render

import foo.starred.athen.annotations.impl.LoadableAthenModule
import foo.starred.athen.modules.impl.render.highlight.MobHighlight

@LoadableAthenModule
object MobHighlightESP {
    val depth by MobHighlight.config.switch("Depth check")
    val tracer by MobHighlight.config.switch("Tracers")
}