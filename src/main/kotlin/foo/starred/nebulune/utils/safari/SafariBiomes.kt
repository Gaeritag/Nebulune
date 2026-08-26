package foo.starred.nebulune.utils.safari

enum class SafariBiome {
    FOREST,
    HAUNTED,
    ICY,
    CAVERN,
    OUTSIDE
}

private const val CENTER_X = -50.0
private const val CENTER_Z = 0.0
private const val SAFARI_RADIUS = 150.0

fun getCritterSafariBiome(x: Double, z: Double): SafariBiome {
    if (x < CENTER_X - SAFARI_RADIUS || x > CENTER_X + SAFARI_RADIUS ||
        z < CENTER_Z - SAFARI_RADIUS || z > CENTER_Z + SAFARI_RADIUS) {
        return SafariBiome.OUTSIDE
    }

    return when {
        x >= CENTER_X && z >= CENTER_Z -> SafariBiome.FOREST
        x >= CENTER_X && z < CENTER_Z -> SafariBiome.HAUNTED
        x < CENTER_X && z >= CENTER_Z -> SafariBiome.CAVERN
        else -> SafariBiome.ICY
    }
}

fun isInCritterSafari(x: Double, z: Double): Boolean {
    return getCritterSafariBiome(x, z) != SafariBiome.OUTSIDE
}