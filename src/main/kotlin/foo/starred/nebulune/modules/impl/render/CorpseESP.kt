package foo.starred.nebulune.modules.impl.render

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.location.island.impl.PresetSkyBlockIsland
import foo.starred.athen.config.dsl.impl.category.ConfigCategory
import foo.starred.athen.events.LocationEvent
import foo.starred.athen.events.MessageEvent
import foo.starred.athen.events.WorldRenderEvent
import foo.starred.athen.modules.Module
import foo.starred.athen.utils.render.renderBoundingBox
import foo.starred.nebulune.utils.extractStyledBox
import foo.starred.nebulune.utils.extractTracer
import foo.starred.parallax.api.primitives.ParallaxText
import foo.starred.snowbird.api.level
import foo.starred.snowbird.api.player
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import java.awt.Color
import kotlin.math.floor

@Load
@OnlyIn(islands = [PresetSkyBlockIsland.MINESHAFT])
object CorpseESP : Module(
    "Corpse ESP",
    "Highlights frozen corpses in Glacite Mineshafts.",
    ConfigCategory.RENDER
) {
    private val highlightStyle by config.selector("Highlight style", listOf("Outline", "Filled", "Both"), 2)
    private val boxType by config.selector("Box type", listOf("Entity bounds", "Block cube"), 0)
    private val lineWidth by config.slider("Line width", 2f, 1f, 10f)
    private val depthTest by config.switch("Depth test", false)
    private val tracer by config.switch("Show tracer", true)
    private val showName by config.switch("Show name", true)
    private val showDistance by config.switch("Show distance", true)
    private val hideClaimed by config.switch("Hide claimed", true)

    private val colors by config.group("Colors")
    private val lapisColor by colors.colorPicker("Lapis color", Color(0, 100, 255).rgb)
    private val tungstenColor by colors.colorPicker("Tungsten color", Color(155, 155, 155).rgb)
    private val umberColor by colors.colorPicker("Umber color", Color(181, 98, 34).rgb)
    private val vanguardColor by colors.colorPicker("Vanguard color", Color(242, 36, 184).rgb)

    private val types by config.group("Corpse types")
    private val lapisToggle by types.switch("Lapis", true)
    private val tungstenToggle by types.switch("Tungsten", true)
    private val umberToggle by types.switch("Umber", true)
    private val vanguardToggle by types.switch("Vanguard", true)

    private val claimed = mutableListOf<Vec3>()

    enum class CorpseType(
        val typeName: String,
        val skyblockId: String,
        val helmetName: String,
    ) {
        LAPIS("Lapis", "LAPIS_ARMOR_HELMET", "Lapis Armor Helmet"),
        TUNGSTEN("Tungsten", "MINERAL_HELMET", "Mineral Helmet"),
        UMBER("Umber", "YOG_HELMET", "Yog Helmet"),
        VANGUARD("Vanguard", "VANGUARD_HELMET", "Vanguard Helmet");

        fun isEnabled(): Boolean = when (this) {
            LAPIS -> lapisToggle
            TUNGSTEN -> tungstenToggle
            UMBER -> umberToggle
            VANGUARD -> vanguardToggle
        }

        fun getColor(): Int = when (this) {
            LAPIS -> lapisColor
            TUNGSTEN -> tungstenColor
            UMBER -> umberColor
            VANGUARD -> vanguardColor
        }

        fun matches(item: ItemStack): Boolean {
            if (item.isEmpty) return false
            val id = item.getSkyBlockId()
            if (id != null && id.equals(skyblockId, ignoreCase = true)) return true
            val clean = item.cleanName
            if (clean.contains(helmetName, ignoreCase = true)) return true
            val hover = item.hoverName.string
            return hover.contains(helmetName, ignoreCase = true)
        }

        companion object {
            fun fromEntity(entity: ArmorStand): CorpseType? {
                if (entity.isInvisible || !entity.isAlive) return null
                if (!entity.hasItemInSlot(EquipmentSlot.HEAD)) return null
                val head = entity.getItemBySlot(EquipmentSlot.HEAD)
                return entries.firstOrNull { it.matches(head) }
            }
        }
    }

    init {
        on<WorldRenderEvent.Extract> {
            val p = player ?: return@on
            val lvl = level ?: return@on

            for (entity in lvl.entitiesForRendering()) {
                val armorStand = entity as? ArmorStand ?: continue
                val type = CorpseType.fromEntity(armorStand) ?: continue
                if (!type.isEnabled()) continue

                val pos = armorStand.position()
                val isClaimed = claimed.any { it.distanceTo(pos) < 5.0 }
                if (isClaimed && hideClaimed) continue

                val color = if (isClaimed) Color(128, 128, 128).rgb else type.getColor()

                val box = when (boxType) {
                    1 -> {
                        val x = floor(armorStand.x)
                        val y = floor(armorStand.y)
                        val z = floor(armorStand.z)
                        AABB(x, y, z, x + 1.0, y + 1.0, z + 1.0)
                    }
                    else -> armorStand.renderBoundingBox
                }

                extractStyledBox(box, color, highlightStyle, lineWidth, depth = depthTest)

                val center = Vec3(
                    (box.minX + box.maxX) / 2.0,
                    (box.minY + box.maxY) / 2.0,
                    (box.minZ + box.maxZ) / 2.0
                )

                if (tracer) {
                    extractTracer(center, color, lineWidth, depthTest = depthTest)
                }

                if (showName || showDistance) {
                    val text = buildString {
                        if (showName) {
                            append(type.typeName)
                            if (isClaimed) append(" (Looted)")
                        }
                        if (showDistance) {
                            val dist = p.distanceTo(armorStand).toInt()
                            if (showName) append(" ")
                            append("§7[${dist}m]")
                        }
                    }
                    val textPos = Vec3(center.x, box.maxY + 0.3, center.z)
                    ParallaxText.string(
                        text,
                        textPos,
                        color,
                        Color(0, 0, 0, 150).rgb,
                        scale = 1.0f,
                        depth = depthTest,
                        shadow = true,
                        increase = true
                    )
                }
            }
        }

        on<MessageEvent.Chat.Receive> {
            if (stripped.contains("CORPSE LOOT!")) {
                val p = player?.position() ?: return@on
                claimed.add(p)
            }
        }

        on<LocationEvent.Server.Connect> {
            claimed.clear()
        }

        on<LocationEvent.Server.Disconnect> {
            claimed.clear()
        }

        on<LocationEvent.SkyBlock.Island> {
            claimed.clear()
        }
    }
}