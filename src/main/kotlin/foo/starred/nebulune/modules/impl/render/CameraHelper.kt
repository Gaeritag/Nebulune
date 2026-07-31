package foo.starred.nebulune.modules.impl.render

import foo.starred.athen.annotations.Load
import foo.starred.athen.config.Category
import foo.starred.athen.modules.Module

@Load
object CameraHelper : Module(
    "Camera helper",
    "QoL additions to the vanilla camera.",
    Category.RENDER
) {
    private val _clip by config.switch("Camera clip")
    private val _dist by config.switch("Custom distance")

    @JvmStatic
    val distance by config.slider("Distance", 4f, 3f, 15f, "blocks", true).dependsOn { _dist }

    @JvmStatic
    val dist: Boolean
        get() = enabled && _dist

    @JvmStatic
    val clip: Boolean
        get() = enabled && _clip
}