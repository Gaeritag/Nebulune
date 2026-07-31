package foo.starred.nebulune.modules.impl.render

import foo.starred.athen.annotations.Load
import foo.starred.athen.modules.impl.render.highlight.MobHighlight

@Load
object MobHighlightESP {
    val depth by MobHighlight.config.switch("Depth check")
    val tracer by MobHighlight.config.switch("Tracers")
}