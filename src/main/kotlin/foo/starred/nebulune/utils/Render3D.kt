package foo.starred.nebulune.utils

import foo.starred.athen.api.rendering.level.impl.extensions.impl.extractLine
import foo.starred.snowbird.api.client
import net.minecraft.world.phys.Vec3

fun extractTracer(to: Vec3, color: Int, lineWidth: Float = 3f, depthTest: Boolean = false) {
    //~ if >= 26.2 'client.gameRenderer.mainCamera' -> 'client.gameRenderer.mainCamera()'
    val camera = client.gameRenderer.mainCamera
    val from = camera.position().add(Vec3.directionFromRotation(camera.xRot(), camera.yRot()))
    extractLine(from.toVector3f(), to.toVector3f(), color, lineWidth, depthTest)
}