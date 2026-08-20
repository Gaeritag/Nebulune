package foo.starred.nebulune.modules.impl.render

import foo.starred.athen.annotations.Load
import foo.starred.athen.modules.impl.render.CameraHelper

@Load
object CameraHelper {
    private val _clip by CameraHelper.config.switch("Camera clip")
    private val _dist by CameraHelper.config.switch("Custom distance")

    @JvmStatic
    val distance by CameraHelper.config.slider("Distance", 4f, 3f, 15f, "blocks", true)

    @JvmStatic
    val dist: Boolean
        get() = CameraHelper.enabled && _dist

    @JvmStatic
    val clip: Boolean
        get() = CameraHelper.enabled && _clip
}