package foo.starred.nebulune.modules.impl.slayer

import foo.starred.athen.annotations.impl.LoadableAthenModule
import foo.starred.athen.modules.impl.slayer.SlayerHighlight

@LoadableAthenModule
object BossESP {
    val depth by SlayerHighlight.config.switch("Depth check", true)
}