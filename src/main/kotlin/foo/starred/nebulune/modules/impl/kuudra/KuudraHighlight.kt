@file:Suppress("ObjectPrivatePropertyName")

package foo.starred.nebulune.modules.impl.kuudra

import foo.starred.athen.annotations.Load
import foo.starred.athen.api.kuudra.KuudraAPI
import foo.starred.athen.events.WorldRenderEvent
import foo.starred.athen.events.core.on
import foo.starred.athen.events.core.runWhen
import foo.starred.athen.modules.impl.kuudra.KuudraInfo
import foo.starred.athen.ui.themes.Catppuccin
import foo.starred.athen.utils.render.renderPos
import foo.starred.nebulune.utils.extractTracer
import foo.starred.snowbird.api.data.Observable.Companion.and
import java.awt.Color

@Load
object KuudraHighlight {
    private val tracer = KuudraInfo.config.switch("Tracer", false).unique("tracer")
    private val `tracer$color` by KuudraInfo.config.colorPicker("Tracer color", Color(Catppuccin.Mocha.Peach.argb, true))
    private val `tracer$width` by KuudraInfo.config.slider("Tracer width", 2f, 1f, 10f)
    private val `tracer$depth` by KuudraInfo.config.switch("Tracer depth")

    init {
        on<WorldRenderEvent.Extract> {
            if (!KuudraAPI.inRun) return@on
            val k = KuudraAPI.kuudra ?: return@on
            extractTracer(k.renderPos, `tracer$color`.rgb, `tracer$width`, `tracer$depth`)
        }.runWhen(KuudraInfo.observable and tracer.state)
    }
}