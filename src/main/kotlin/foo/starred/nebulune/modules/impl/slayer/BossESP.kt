package foo.starred.nebulune.modules.impl.slayer

import foo.starred.athen.annotations.Load
import foo.starred.athen.modules.impl.slayer.SlayerHighlight

@Load
object BossESP {
    val depth by SlayerHighlight.config.switch("Depth check", true)
}