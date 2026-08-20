package foo.starred.nebulune.modules.impl.render.worldScanner

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.location.SkyBlockIsland
import foo.starred.athen.api.messaging.impl.MessagingAPI.mod
import foo.starred.athen.api.rendering.level.impl.extensions.impl.extractStyledBox
import foo.starred.athen.api.rendering.level.impl.extensions.impl.extractText
import foo.starred.athen.config.Category
import foo.starred.athen.config.dsl.impl.builders.group.ConfigGroupBuilder
import foo.starred.athen.events.LocationEvent
import foo.starred.athen.events.WorldRenderEvent
import foo.starred.athen.modules.Module
import foo.starred.nebulune.events.ClientChunkEvent
import foo.starred.nebulune.utils.extractTracer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.phys.AABB
import java.awt.Color

//? if >= 26.2
//import net.minecraft.world.phys.Vec3

@Load
@OnlyIn(islands = [SkyBlockIsland.CRYSTAL_HOLLOWS])
object WorldScanner: Module(
    "World Scanner",
    "Scan Crystal Hollow world for structures",
    Category.RENDER
) {
    data class StructureEspConfig(
        val expandable: ConfigGroupBuilder,
        val enable: () -> Boolean,
        val highlightStyle: () -> Int,
        val color: () -> Color,
        val tracer: () -> Boolean,
        val displayName: () -> Boolean,
        val displayScale: () -> Float,
        val displayBackgroundOpacity: () -> Float,
        val sendCoordsInChat: () -> Boolean,
    )

    fun createStructureEspConfig(
        group: ConfigGroupBuilder,
        defaultColor: Color,
    ): StructureEspConfig {
        val key = group.key

        val enable by group.switch("Enable", true)
            .unique(key + "Enable")

        val highlightStyle by group.selector("Highlight Style", listOf("Outline", "Filled", "Both"), 2)
            .unique(key + "Highlight Style")

        val color by group.colorPicker("ESP Color", defaultColor)
            .unique(key + "ESP Color")

        val tracer by group.switch("Tracer", false)
            .unique(key + "Tracer")

        val displayName by group.switch("Display Name", true)
            .unique(key + "Display Name")

        val displayScale by group.slider("Name Scale", 1f, 0f, 1f, double = true)
            .unique(key + "Name Scale")

        val displayBackgroundOpacity by group.slider("Display Background Opacity", 0.5f, 0f, 1f, double = true)
            .unique(key + "Display Background Opacity")

        val sendCoordsInChat by group.switch("Send Coords In Chat", true)
            .unique(key + "Send Coords In Chat")

        return StructureEspConfig(
            group,
            { enable },
            { highlightStyle },
            { color },
            { tracer },
            { displayName },
            { displayScale },
            { displayBackgroundOpacity },
            { sendCoordsInChat },
        )
    }

    private val grotto by config.group("Fairy Grotto")
    val grottoConfig = createStructureEspConfig(grotto, Color(255, 85, 255))
    val grottoConfigShowNumberOfBlocks by grotto.switch("Show Number of Blocks", true)
    val grottoConfigShowNumberOfBlocksBackgroundOpacity by grotto.slider("Text Background Opacity", 0.5f, 0f, 1f, double = true)

    private val sapphire by config.group("Sapphire Crystal")
    val sapphireConfig = createStructureEspConfig(sapphire, Color(85, 255, 255))

    private val amber by config.group("Amber Crystal")
    val amberConfig = createStructureEspConfig(amber, Color(255, 170, 0))

    private val amethyst by config.group("Amethyst Crystal")
    val amethystConfig = createStructureEspConfig(amethyst, Color(170, 0, 170))

    private val jade by config.group("Jade Crystal")
    val jadeConfig = createStructureEspConfig(jade, Color(0, 170, 0))

    private val topaz by config.group("Topaz Crystal")
    val topazConfig = createStructureEspConfig(topaz, Color(255, 255, 85))

    private val corleone by config.group("Corleone")
    val corleoneConfig = createStructureEspConfig(corleone, Color(85, 255, 85))

    private val goldenDragon by config.group("Golden Dragon")
    val goldenDragonConfig = createStructureEspConfig(goldenDragon, Color(255, 255, 255))

    private val keyGuardian by config.group("Key Guardian")
    val keyGuardianConfig = createStructureEspConfig(keyGuardian, Color(170, 0, 170))

    private val xalx by config.group("Xalx")
    val xalxConfig = createStructureEspConfig(xalx, Color(80, 110, 0))

    private val pete by config.group("Pete")
    val peteConfig = createStructureEspConfig(pete, Color(110, 42, 0))

    private val odawa by config.group("Odawa")
    val odawaConfig = createStructureEspConfig(odawa, Color(170, 170, 170))

    private val wormFishing by config.group("Worm Fishing")
    val wormFishingConfig = createStructureEspConfig(wormFishing, Color(255, 85, 85))

    private val grottos = mutableListOf<Triple<Pair<Int, Int>, BlockPos, Int>>()
    private val structures = mutableListOf<Pair<Structure, Triple<Int, Int, Int>>>()
    private val scannedChunks = mutableListOf<Pair<Int, Int>>()
    private val grottoChunksMap = mutableMapOf<Pair<Int, Int>, Triple<Pair<Int, Int>, BlockPos, Int>>()

    private val scope = CoroutineScope(Dispatchers.Default.limitedParallelism(1))

    init {
        on<ClientChunkEvent.Load> {
            if (Pair(chunk.pos.x, chunk.pos.z) !in scannedChunks) scope.launch { scanChunk(chunk) }
            else scannedChunks.add(Pair(chunk.pos.x, chunk.pos.z))
        }

        on<LocationEvent.Server.Connect> {
            grottos.clear()
            structures.clear()
            scannedChunks.clear()
            grottoChunksMap.clear()
        }

        on<WorldRenderEvent.Extract> {
            if (grottoConfig.enable()) {
                for (grotto in grottos) {
                    val blockPos = grotto.second
                    //~ if >= 26.2 'blockPos.center' -> 'Vec3.atCenterOf(blockPos)'
                    val center = blockPos.center
                    val aabb = AABB(blockPos)
                    val color = grottoConfig.color()

                    extractStyledBox(aabb, color.rgb, grottoConfig.highlightStyle(), depth = false)
                    if (grottoConfig.tracer()) extractTracer(center, grottoConfig.color().rgb, 2f, false)
                    if (grottoConfig.displayName()) extractText("Fairy Grotto",
                        center.add(0.0, 10.0, 0.0),
                        grottoConfig.color().rgb,
                        Color(0, 0, 0, (255 * grottoConfig.displayBackgroundOpacity()).toInt()).rgb,
                        grottoConfig.displayScale(),
                        depth = false,
                        shadow = true,
                        increase = true
                    )
                    if (grottoConfigShowNumberOfBlocks) extractText(
                        grotto.third.toString(),
                        center,
                        grottoConfig.color().rgb,
                        Color(0, 0, 0, (255 * grottoConfigShowNumberOfBlocksBackgroundOpacity).toInt()).rgb,
                        grottoConfig.displayScale(),
                        depth = false,
                        shadow = true,
                        increase = true
                    )
                }
            }

            for (structure in structures) {
                if (!structure.first.config.enable()) continue
                val structureConfig = structure.first.config
                val pos = structure.second
                val blockPos = BlockPos(pos.first, pos.second, pos.third)
                val aabb = AABB(blockPos)
                val color = structureConfig.color()
                extractStyledBox(aabb, color.rgb, structureConfig.highlightStyle(), depth = false)
                //~ if >= 26.2 'blockPos.center' -> 'Vec3.atCenterOf(blockPos)' {
                if (structureConfig.tracer()) extractTracer(blockPos.center, structureConfig.color().rgb, 2f, false)
                if (structureConfig.displayName()) extractText(structure.first.displayName, blockPos.center, structureConfig.color().rgb, Color(0, 0, 0, (255 * structureConfig.displayBackgroundOpacity()).toInt()).rgb, structureConfig.displayScale(), depth = false, shadow = true, increase = true)
                //~ }
            }
        }
    }

    private fun scanStructure(
        chunk: LevelChunk,
        structure: Structure,
        x: Int,
        y: Int,
        z: Int
    ): Boolean {
        if (structure == Structure.FAIRY_GROTTO) return false

        val worldX = chunk.pos.x * 16 + x
        val worldZ = chunk.pos.z * 16 + z
        val worldPos = BlockPos(worldX, y, worldZ)

        if (structure == Structure.WORM_FISHING && (x < 513 || y < 80 || z < 513)) return false
        if (!structure.quarter.testPredicate(worldPos)) return false

        val blockPos = BlockPos.MutableBlockPos()
        for (structureY in structure.blocks.indices) {
            blockPos.set(x, y + structureY, z)
            val (block, enumProperty, expectedValue) = structure.blocks[structureY]
            if (block == null) continue

            val worldState = chunk.getBlockState(blockPos)
            if (!worldState.`is`(block)) return false

            if (enumProperty != null && expectedValue != null) {
                if (
                    !worldState.hasProperty(enumProperty) ||
                    worldState.getValue(enumProperty) != expectedValue
                ) {
                    return false
                }
            }
        }

        return true
    }

    private fun getAllNearbyGrottoChunks(x: Int, z: Int): MutableList<Triple<Pair<Int, Int>, BlockPos, Int>> {
        val result = mutableListOf<Triple<Pair<Int, Int>, BlockPos, Int>>()
        val visited = mutableSetOf<Pair<Int, Int>>()
        val queue = ArrayDeque<Pair<Int, Int>>()

        queue.add(x to z)

        while (queue.isNotEmpty()) {
            val (cx, cz) = queue.removeFirst()
            val key = cx to cz

            if (!visited.add(key)) continue

            val current = grottoChunksMap[key] ?: continue
            result.add(current)

            for (dx in -1..1) {
                for (dz in -1..1) {
                    if (dx == 0 && dz == 0) continue
                    queue.add(cx + dx to cz + dz)
                }
            }
        }

        return result
    }

    private fun scanChunk(chunk: LevelChunk) {
        val structuresToScan = mutableListOf<Structure>()

        for (structure in Structure.entries) {
            if (structure.config.enable() && !structures.any { it.first == structure }) {
                structuresToScan.add(structure)
            }
        }

        val worldPos = BlockPos.MutableBlockPos()
        val chunkPos = BlockPos.MutableBlockPos()
        val chunkJasperBlocks = mutableListOf<BlockPos>()
        val chunkX = chunk.pos.x
        val chunkZ = chunk.pos.z

        val foundStructure = structures.mapTo(mutableSetOf()) { it.first }

        for (x in 0..15) {
            for (z in 0..15) {
                for (y in 0..169) {
                    val worldX = chunkX * 16 + x
                    val worldZ = chunkZ * 16 + z

                    worldPos.set(worldX, y, worldZ)

                    for (structureToScan in structuresToScan) {
                        if (structureToScan in foundStructure) continue
                        if (!scanStructure(chunk, structureToScan, x, y, z)) continue

                        foundStructure.add(structureToScan)
                        if (structureToScan.config.sendCoordsInChat()) "${structureToScan.displayName} found at x: $worldX, y: $y, z: $worldZ".mod()

                        structures.add(
                            structureToScan to Triple(
                                worldX + structureToScan.offsetX,
                                y + structureToScan.offsetY,
                                worldZ + structureToScan.offsetZ
                            )
                        )
                    }

                    if (grottoConfig.enable()) {
                        chunkPos.set(x, y, z)
                        val state = chunk.getBlockState(chunkPos)

                        //~ if >= 26.2 'Blocks.MAGENTA_STAINED_GLASS' -> 'Blocks.STAINED_GLASS.magenta()'
                        //~ if >= 26.2 'Blocks.MAGENTA_STAINED_GLASS_PANE' -> 'Blocks.STAINED_GLASS_PANE.magenta()'
                        if (state.`is`(Blocks.MAGENTA_STAINED_GLASS_PANE) || state.`is`(Blocks.MAGENTA_STAINED_GLASS)) {
                            worldPos.set(chunk.pos.x * 16 + x, y, chunk.pos.z * 16 + z)
                            if (!CrystalHollowsQuarter.NUCLEUS.testPredicate(worldPos)) chunkJasperBlocks.add(worldPos.immutable())
                        }
                    }
                }
            }
        }


        if (chunkJasperBlocks.isEmpty()) return
        val size = chunkJasperBlocks.size

        val center = BlockPos(
            (chunkJasperBlocks.sumOf { it.x }.toDouble() / size).toInt(),
            (chunkJasperBlocks.sumOf { it.y }.toDouble() / size).toInt(),
            (chunkJasperBlocks.sumOf { it.z }.toDouble() / size).toInt()
        )

        if (CrystalHollowsQuarter.NUCLEUS.testPredicate(center)) return

        grottoChunksMap[Pair(chunkX, chunkZ)] = Triple(Pair(chunkX, chunkZ), center, chunkJasperBlocks.size)

        val cluster = getAllNearbyGrottoChunks(chunkX, chunkZ)
        if (cluster.isEmpty()) return

        val merged = BlockPos(
            cluster.sumOf { it.second.x } / cluster.size,
            cluster.sumOf { it.second.y } / cluster.size,
            cluster.sumOf { it.second.z } / cluster.size
        )

        val numGrottos = grottos.size

        grottos.removeIf { grotto ->
            cluster.any { it.first.first == grotto.first.first && it.first.second == grotto.first.second }
        }

        grottos.add(Triple(Pair(chunkX, chunkZ), merged, cluster.sumOf { it.third }))

        if (grottoConfig.sendCoordsInChat() && numGrottos != grottos.size) "Fairy Grotto found at x: ${merged.x}, y: ${merged.y}, z: ${merged.z}".mod()
    }
}