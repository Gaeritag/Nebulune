package foo.starred.nebulune.modules.impl.render.worldScanner

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.location.SkyBlockIsland
import foo.starred.athen.api.rendering.level.impl.extensions.impl.extractStyledBox
import foo.starred.athen.api.rendering.level.impl.extensions.impl.extractText
import foo.starred.athen.config.Category
import foo.starred.athen.config.ExpandableHandle
import foo.starred.athen.events.LocationEvent
import foo.starred.athen.events.WorldRenderEvent
import foo.starred.athen.handlers.Notifier.notify
import foo.starred.athen.handlers.Typo.modMessage
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

@Load
@OnlyIn(islands = [SkyBlockIsland.CRYSTAL_HOLLOWS])
object WorldScanner: Module(
    "World Scanner",
    "Scan Crystal Hollow world for structures",
    Category.RENDER
) {
    data class StructureEspConfig(
        val expandable: ExpandableHandle,
        val enable: () -> Boolean,
        val highlightStyle: () -> Int,
        val color: () -> Color,
        val tracer: () -> Boolean,
        val displayName: () -> Boolean,
        val displayScale: () -> Float,
        val displayBackgroundOpacity: () -> Float,
        val showNotification: () -> Boolean,
        val sendCoordsInChat: () -> Boolean,
    )

    fun createStructureEspConfig(
        expandable: ExpandableHandle,
        defaultColor: Color,
    ): StructureEspConfig {
        val key = expandable.key

        val enable by config.switch("Enable", true)
            .unique(key + "Enable")
            .childOf { expandable }

        val highlightStyle by config.dropdown("Highlight Style", listOf("Outline", "Filled", "Both"), 2)
            .unique(key + "Highlight Style")
            .childOf { expandable }

        val color by config.colorPicker("ESP Color", defaultColor)
            .unique(key + "ESP Color")
            .childOf { expandable }

        val tracer by config.switch("Tracer", false)
            .unique(key + "Tracer")
            .childOf { expandable }

        val displayName by config.switch("Display Name", true)
            .unique(key + "Display Name")
            .childOf { expandable }

        val displayScale by config.slider("Name Scale", 1f, 0f, 1f, showDouble = true)
            .unique(key + "Name Scale")
            .dependsOn { displayName }
            .childOf { expandable }

        val displayBackgroundOpacity by config.slider("Display Background Opacity", 0.5f, 0f, 1f, showDouble = true)
            .unique(key + "Display Background Opacity")
            .dependsOn { displayName }
            .childOf { expandable }

        val notification by config.switch("Show Notification", true)
            .unique(key + "Show Notification")
            .childOf { expandable }

        val sendCoordsInChat by config.switch("Send Coords In Chat", true)
            .unique(key + "Send Coords In Chat")
            .childOf { expandable }

        return StructureEspConfig(
            expandable,
            { enable },
            { highlightStyle },
            { color },
            { tracer },
            { displayName },
            { displayScale },
            { displayBackgroundOpacity },
            { notification },
            { sendCoordsInChat },
        )
    }

    private val grottoExpandable by config.expandable("Fairy Grotto")
    val grottoConfig = createStructureEspConfig(
        grottoExpandable,
        Color(255, 85, 255)
    )
    val grottoConfigShowNumberOfBlocks by config.switch("Show Number of Blocks", true).childOf { grottoExpandable }
    val grottoConfigShowNumberOfBlocksBackgroundOpacity by config.slider("Text Background Opacity", 0.5f, 0f, 1f, showDouble = true).dependsOn { grottoConfigShowNumberOfBlocks }.childOf { grottoExpandable }

    private val sapphireExpandable by config.expandable("Sapphire Crystal")
    val sapphireConfig = createStructureEspConfig(
        sapphireExpandable,
        Color(85, 255, 255)
    )

    private val amberExpandable by config.expandable("Amber Crystal")
    val amberConfig = createStructureEspConfig(
        amberExpandable,
        Color(255, 170, 0)
    )

    private val amethystExpandable by config.expandable("Amethyst Crystal")
    val amethystConfig = createStructureEspConfig(
        amethystExpandable,
        Color(170, 0, 170)
    )

    private val jadeExpandable by config.expandable("Jade Crystal")
    val jadeConfig = createStructureEspConfig(
        jadeExpandable,
        Color(0, 170, 0)
    )

    private val topazExpandable by config.expandable("Topaz Crystal")
    val topazConfig = createStructureEspConfig(
        topazExpandable,
        Color(255, 255, 85)
    )

    private val corleoneExpandable by config.expandable("Corleone")
    val corleoneConfig = createStructureEspConfig(
        corleoneExpandable,
        Color(85, 255, 85)
    )

    private val goldenDragonExpandable by config.expandable("Golden Dragon")
    val goldenDragonConfig = createStructureEspConfig(
        goldenDragonExpandable,
        Color(255, 255, 255)
    )

    private val keyGuardianExpandable by config.expandable("Key Guardian")
    val keyGuardianConfig = createStructureEspConfig(
        keyGuardianExpandable,
        Color(170, 0, 170)
    )

    private val xalxExpandable by config.expandable("Xalx")
    val xalxConfig = createStructureEspConfig(
        xalxExpandable,
        Color(80, 110, 0)
    )

    private val peteExpandable by config.expandable("Pete")
    val peteConfig = createStructureEspConfig(
        peteExpandable,
        Color(110, 42, 0)
    )

    private val odawaExpandable by config.expandable("Odawa")
    val odawaConfig = createStructureEspConfig(
        odawaExpandable,
        Color(170, 170, 170)
    )

    private val wormFishingExpandable by config.expandable("Worm Fishing")
    val wormFishingConfig = createStructureEspConfig(
        wormFishingExpandable,
        Color(255, 85, 85)
    )

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
                if (structureConfig.tracer()) extractTracer(blockPos.center, structureConfig.color().rgb, 2f, false)
                if (structureConfig.displayName()) extractText(
                    structure.first.displayName,
                    blockPos.center,
                    structureConfig.color().rgb,
                    Color(0, 0, 0, (255 * structureConfig.displayBackgroundOpacity()).toInt()).rgb,
                    structureConfig.displayScale(),
                    depth = false,
                    shadow = true,
                    increase = true
                )
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
                        if (structureToScan.config.showNotification()) (structureToScan.displayName + " Found").notify(duration = 5000)
                        if (structureToScan.config.sendCoordsInChat()) "${structureToScan.displayName} found at x: $worldX, y: $y, z: $worldZ".modMessage()

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

                        if (
                            state.`is`(Blocks.MAGENTA_STAINED_GLASS_PANE) ||
                            state.`is`(Blocks.MAGENTA_STAINED_GLASS)
                        ) {
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

        if (grottoConfig.showNotification() && numGrottos != grottos.size) (Structure.FAIRY_GROTTO.displayName + " Found").notify(duration = 5000)
        if (grottoConfig.sendCoordsInChat() && numGrottos != grottos.size) "Fairy Grotto found at x: ${merged.x}, y: ${merged.y}, z: ${merged.z}".modMessage()
    }
}