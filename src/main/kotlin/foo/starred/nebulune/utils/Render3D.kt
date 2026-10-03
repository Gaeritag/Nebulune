package foo.starred.nebulune.utils

import foo.starred.parallax.api.primitives.ParallaxBox
import foo.starred.parallax.api.primitives.ParallaxLine
import foo.starred.snowbird.api.client
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

fun extractTracer(to: Vec3, color: Int, lineWidth: Float = 3f, depthTest: Boolean = false) {
    //~ if >= 26.2 'client.gameRenderer.mainCamera' -> 'client.gameRenderer.mainCamera()'
    val camera = client.gameRenderer.mainCamera
    val from = camera.position().add(Vec3.directionFromRotation(camera.xRot(), camera.yRot()))
    ParallaxLine.singular(from.toVector3f(), to.toVector3f(), color, lineWidth, depthTest)
}

fun extractStyledBox(
    aabb: AABB,
    color: Int,
    style: Int = 2,
    width: Float = 2f,
    depth: Boolean = true
) {
    when (style) {
        0 -> ParallaxBox.frame(aabb, color, width, depth)
        1 -> ParallaxBox.fill(aabb, color, depth)
        2 -> {
            ParallaxBox.fill(aabb, color and 0x00FFFFFF or ((color ushr 25) shl 24), depth)
            ParallaxBox.frame(aabb, color, width, depth)
        }
    }
}