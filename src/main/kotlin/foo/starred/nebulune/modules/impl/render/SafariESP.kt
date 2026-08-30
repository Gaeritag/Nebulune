package foo.starred.nebulune.modules.impl.render

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.location.LocationAPI
import foo.starred.athen.api.location.SkyBlockIsland
import foo.starred.athen.api.rendering.level.impl.extensions.impl.extractFrameBox
import foo.starred.athen.api.rendering.level.impl.extensions.impl.extractStyledBox
import foo.starred.athen.config.Category
import foo.starred.athen.events.WorldRenderEvent
import foo.starred.athen.modules.Module
import foo.starred.athen.utils.render.renderBoundingBox
import foo.starred.nebulune.utils.getSkinTexture
import foo.starred.nebulune.utils.safari.*
import foo.starred.snowbird.api.level
import foo.starred.snowbird.api.player
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Entity
//? if >= 26.2
//import net.minecraft.world.entity.EntityTypes as EntityType
//? if = 26.1
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.animal.fish.TropicalFish
import net.minecraft.world.entity.animal.parrot.Parrot
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.monster.Shulker
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.ShulkerBoxBlock
import net.minecraft.world.phys.AABB
import tech.thatgravyboat.skyblockapi.utils.extentions.getTexture
import java.awt.Color

@Load
@OnlyIn(islands = [SkyBlockIsland.SAFARI])
object SafariESP : Module(
    "Safari ESP",
    "ESP for Safari mobs",
    Category.RENDER
) {
    // FLOOR DROPS
    private val floorDropGroup by config.group("Floor Drops")
    val `floorDrop$toggle` by floorDropGroup.switch("Toggle Floor Drops")
    val `floorDrop$only_in_biome` by floorDropGroup.switch("Toggle floor drops only in current biome")
    val `floorDrop$color` by floorDropGroup.colorPicker("Color", Color(0, 255, 0))

    // CAVERN
    private val cavernGroup by config.group("Cavern Biome")
    val `cavern$toggle` by cavernGroup.switch("Toggle cavern")
    val `cavern$only_in_biome` by cavernGroup.switch("Toggle cavern mobs only in cavern biome")
    val `cavern$cavernrnfish` by cavernGroup.switch("cavernfish")
    val `cavern$cavernrnfish_color` by cavernGroup.colorPicker("Color", Color(180, 100, 30))
    val `cavern$flitter` by cavernGroup.switch("Flitter")
    val `cavern$flitter_color` by cavernGroup.colorPicker("Color", Color(40, 90, 110))
    val `cavern$shyworm` by cavernGroup.switch("Shyworm")
    val `cavern$shyworm_color` by cavernGroup.colorPicker("Color", Color(80, 160, 50))
    val `cavern$driftling` by cavernGroup.switch("Driftling")
    val `cavern$driftling_color` by cavernGroup.colorPicker("Color", Color(150, 110, 60))
    val `cavern$chuckwalla` by cavernGroup.switch("Chuckwalla")
    val `cavern$chuckwalla_color` by cavernGroup.colorPicker("Color", Color(60, 50, 40))
    val `cavern$rockmite` by cavernGroup.switch("Rockmite")
    val `cavern$rockmite_color` by cavernGroup.colorPicker("Color", Color(160, 160, 160))
    val `cavern$scrappy` by cavernGroup.switch("Scrappy")
    val `cavern$scrappy_color` by cavernGroup.colorPicker("Color", Color(190, 120, 130))
    val `cavern$snoozle` by cavernGroup.switch("Snoozle")
    val `cavern$snoozle_color` by cavernGroup.colorPicker("Color", Color(160, 40, 40))
    val `cavern$gemzie` by cavernGroup.switch("Gemzie")
    val `cavern$gemzie_color` by cavernGroup.colorPicker("Color", Color(150, 180, 240))

    // FOREST
    private val forestGroup by config.group("Forest Biome")
    val `forest$toggle` by forestGroup.switch("Toggle Forest")
    val `forest$only_in_biome` by forestGroup.switch("Toggle forest mobs only in forest biome")
    val `forest$foxtrot` by forestGroup.switch("Foxtrot")
    val `forest$foxtrot_color` by forestGroup.colorPicker("Color", Color(240, 110, 20))
    val `forest$bluebird` by forestGroup.switch("Bluebird")
    val `forest$bluebird_color` by forestGroup.colorPicker("Color", Color(20, 50, 200))
    val `forest$honeybug` by forestGroup.switch("Honeybug")
    val `forest$honeybug_color` by forestGroup.colorPicker("Color", Color(240, 190, 30))
    val `forest$treefrog` by forestGroup.switch("Treefrog")
    val `forest$treefrog_color` by forestGroup.colorPicker("Color", Color(110, 130, 90))
    val `forest$woodchucker` by forestGroup.switch("Woodchucker")
    val `forest$woodchucker_color` by forestGroup.colorPicker("Color", Color(80, 70, 70))
    val `forest$fluffling` by forestGroup.switch("Fluffling")
    val `forest$fluffling_color` by forestGroup.colorPicker("Color", Color(230, 230, 230))
    val `forest$hideonfloor` by forestGroup.switch("Hideonfloor")
    val `forest$hideonfloor_color` by forestGroup.colorPicker("Color", Color(100, 130, 40))
    val `forest$parakeet` by forestGroup.switch("Parakeet")
    val `forest$parakeet_color` by forestGroup.colorPicker("Color", Color(90, 200, 50))
    val `forest$macaw` by forestGroup.switch("Macaw")
    val `forest$macaw_color` by forestGroup.colorPicker("Color", Color(210, 30, 30))

    // HAUNTED
    private val hauntedGroup by config.group("Haunted Biome")
    val `haunted$toggle` by hauntedGroup.switch("Toggle Haunted")
    val `haunted$only_in_biome` by hauntedGroup.switch("Toggle haunted mobs only in haunted biome")
    val `haunted$areita` by hauntedGroup.switch("Areita")
    val `haunted$areita_color` by hauntedGroup.colorPicker("Color", Color(20, 90, 90))
    val `haunted$bloodbat` by hauntedGroup.switch("Bloodbat")
    val `haunted$bloodbat_color` by hauntedGroup.colorPicker("Color", Color(90, 40, 30))
    val `haunted$duplico` by hauntedGroup.switch("Duplico")
    val `haunted$duplico_color` by hauntedGroup.colorPicker("Color", Color(120, 120, 120))
    val `haunted$gazer` by hauntedGroup.switch("Gazer")
    val `haunted$gazer_color` by hauntedGroup.colorPicker("Color", Color(30, 50, 70))
    val `haunted$litterbug` by hauntedGroup.switch("Litterbug")
    val `haunted$litterbug_color` by hauntedGroup.colorPicker("Color", Color(130, 50, 170))
    val `haunted$solsnatcher` by hauntedGroup.switch("Solsnatcher")
    val `haunted$solsnatcher_color` by hauntedGroup.colorPicker("Color", Color(50, 60, 130))
    val `haunted$gimmiegold` by hauntedGroup.switch("Gimmiegold")
    val `haunted$gimmiegold_color` by hauntedGroup.colorPicker("Color", Color(255, 210, 0))
    val `haunted$hideonwall` by hauntedGroup.switch("Hideonwall")
    val `haunted$hideonwall_color` by hauntedGroup.colorPicker("Color", Color(140, 70, 140))
    val `haunted$hideyho` by hauntedGroup.switch("Hideyho")
    val `haunted$hideyho_color` by hauntedGroup.colorPicker("Color", Color(190, 190, 190))
    val `haunted$doomspiral` by hauntedGroup.switch("Doomspiral")
    val `haunted$doomspiral_color` by hauntedGroup.colorPicker("Color", Color(20, 140, 150))

    // ICY
    private val icyGroup by config.group("Icy Biome")
    val `icy$toggle` by icyGroup.switch("Toggle Icy")
    val `icy$only_in_biome` by icyGroup.switch("Toggle icy mobs only in icy biome")
    val `icy$strongarm` by icyGroup.switch("Strongarm")
    val `icy$strongarm_color` by icyGroup.colorPicker("Color", Color(220, 120, 20))
    val `icy$tepid` by icyGroup.switch("Tepid")
    val `icy$tepid_color` by icyGroup.colorPicker("Color", Color(200, 210, 200))
    val `icy$polaris` by icyGroup.switch("Polaris")
    val `icy$polaris_color` by icyGroup.colorPicker("Color", Color(255, 255, 255))
    val `icy$shuddersquid` by icyGroup.switch("Shuddersquid")
    val `icy$shuddersquid_color` by icyGroup.colorPicker("Color", Color(50, 190, 180))
    val `icy$billygoat` by icyGroup.switch("Billygoat")
    val `icy$billygoat_color` by icyGroup.colorPicker("Color", Color(230, 220, 200))
    val `icy$mantis_shrimp` by icyGroup.switch("Mantis Shrimp")
    val `icy$mantis_shrimp_color` by icyGroup.colorPicker("Color", Color(40, 90, 100))
    val `icy$nozzlenose` by icyGroup.switch("Nozzlenose")
    val `icy$nozzlenose_color` by icyGroup.colorPicker("Color", Color(140, 150, 170))
    val `icy$troodon` by icyGroup.switch("Troodon")
    val `icy$troodon_color` by icyGroup.colorPicker("Color", Color(120, 80, 190))
    val `icy$wumpa` by icyGroup.switch("Wumpa")
    val `icy$wumpa_color` by icyGroup.colorPicker("Color", Color(100, 90, 90))

    private class BiomeConfig(
        val isEnabled: () -> Boolean,
        val isOnlyInBiome: () -> Boolean,
        val mobs: List<SafariMob>
    )

    private val safariRegistry: Map<SafariBiome, BiomeConfig> = mapOf(
        SafariBiome.CAVERN to BiomeConfig({ `cavern$toggle` }, { `cavern$only_in_biome` }, listOf(
            SafariMob(MobIdentifier.SpecificTropicalFish("CLAYFISH", "GRAY", "BROWN"), { `cavern$cavernrnfish` }, { `cavern$cavernrnfish_color` }),
            SafariMob(MobIdentifier.TexturedItemDisplay(SafariTextures.FLITTER), { `cavern$flitter` }, { `cavern$flitter_color` }),
            SafariMob(MobIdentifier.TexturedHead(SafariTextures.SHYWORM), { `cavern$shyworm` }, { `cavern$shyworm_color` }),
            SafariMob(MobIdentifier.TexturedHead(SafariTextures.DRIFTLING), { `cavern$driftling` }, { `cavern$driftling_color` }),
            SafariMob(MobIdentifier.TexturedItemDisplay(SafariTextures.CHUCKWALLA), { `cavern$chuckwalla` }, { `cavern$chuckwalla_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.SILVERFISH), { `cavern$rockmite` }, { `cavern$rockmite_color` }),
            SafariMob(MobIdentifier.TexturedItemDisplay(SafariTextures.ROCKMITE_DISPLAY), { `cavern$rockmite` }, { `cavern$rockmite_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.ARMADILLO), { `cavern$scrappy` }, { `cavern$scrappy_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.SNIFFER), { `cavern$snoozle` }, { `cavern$snoozle_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.VEX), { `cavern$gemzie` }, { `cavern$gemzie_color` })
        )),
        SafariBiome.FOREST to BiomeConfig({ `forest$toggle` }, { `forest$only_in_biome` }, listOf(
            SafariMob(MobIdentifier.VanillaEntity(EntityType.FOX), { `forest$foxtrot` }, { `forest$foxtrot_color` }),
            SafariMob(MobIdentifier.SpecificParrot(Parrot.Variant.BLUE), { `forest$bluebird` }, { `forest$bluebird_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.BEE), { `forest$honeybug` }, { `forest$honeybug_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.FROG), { `forest$treefrog` }, { `forest$treefrog_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.CREAKING), { `forest$woodchucker` }, { `forest$woodchucker_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.PANDA), { `forest$fluffling` }, { `forest$fluffling_color` }),
            SafariMob(MobIdentifier.ColoredShulker(DyeColor.GREEN), { `forest$hideonfloor` }, { `forest$hideonfloor_color` }),
            SafariMob(MobIdentifier.SpecificParrot(Parrot.Variant.GREEN), { `forest$parakeet` }, { `forest$parakeet_color` }),
            SafariMob(MobIdentifier.SpecificParrot(Parrot.Variant.RED_BLUE), { `forest$macaw` }, { `forest$macaw_color` })
        )),
        SafariBiome.HAUNTED to BiomeConfig({ `haunted$toggle` }, { `haunted$only_in_biome` }, listOf(
            SafariMob(MobIdentifier.VanillaEntity(EntityType.CAVE_SPIDER), { `haunted$areita` }, { `haunted$areita_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.BAT), { `haunted$bloodbat` }, { `haunted$bloodbat_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.INTERACTION), { `haunted$duplico` }, { `haunted$duplico_color` }),
            SafariMob(MobIdentifier.TexturedHead(SafariTextures.GAZER), { `haunted$gazer` }, { `haunted$gazer_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.ENDERMITE), { `haunted$litterbug` }, { `haunted$litterbug_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.PHANTOM), { `haunted$solsnatcher` }, { `haunted$solsnatcher_color` }),
            SafariMob(MobIdentifier.TexturedItemDisplay(SafariTextures.GIMMIEGOLD), { `haunted$gimmiegold` }, { `haunted$gimmiegold_color` }),
            SafariMob(MobIdentifier.ColoredShulker(DyeColor.PURPLE), { `haunted$hideonwall` }, { `haunted$hideonwall_color` }),
            SafariMob(MobIdentifier.PlayerSkin(SafariTextures.HIDEYHO), { `haunted$hideyho` }, { `haunted$hideyho_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.WARDEN), { `haunted$doomspiral` }, { `haunted$doomspiral_color` })
        )),
        SafariBiome.ICY to BiomeConfig({ `icy$toggle` }, { `icy$only_in_biome` }, listOf(
            SafariMob(MobIdentifier.VanillaEntity(EntityType.SNOW_GOLEM), { `icy$strongarm` }, { `icy$strongarm_color` }),
            SafariMob(MobIdentifier.SpecificTropicalFish("SNOOPER", "WHITE", "WHITE"), { `icy$tepid` }, { `icy$tepid_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.POLAR_BEAR), { `icy$polaris` }, { `icy$polaris_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.GLOW_SQUID), { `icy$shuddersquid` }, { `icy$shuddersquid_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.GOAT), { `icy$billygoat` }, { `icy$billygoat_color` }),
            SafariMob(MobIdentifier.TexturedItemDisplay(SafariTextures.MANTIS_SHRIMP), { `icy$mantis_shrimp` }, { `icy$mantis_shrimp_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.DOLPHIN), { `icy$nozzlenose` }, { `icy$nozzlenose_color` }),
            SafariMob(MobIdentifier.TexturedItemDisplay(SafariTextures.TROODON), { `icy$troodon` }, { `icy$troodon_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.RAVAGER), { `icy$wumpa` }, { `icy$wumpa_color` })
        ))
    )

    private fun isMatchingEntity(e: Entity, identifier: MobIdentifier): Boolean {
        return when (identifier) {
            is MobIdentifier.VanillaEntity -> {
                if (e.type != identifier.type) return false
                !(e.type == EntityType.SILVERFISH && (e.isInvisible || e.passengers.isNotEmpty()))
            }
            is MobIdentifier.ColoredShulker -> {
                when (e) {
                    is Shulker -> e.color == identifier.color
                    is Display.BlockDisplay -> {
                        val block = e.blockState.block
                        block is ShulkerBoxBlock && block.color == identifier.color
                    }
                    is Display.ItemDisplay -> {
                        val itemBlock = Block.byItem(e.itemStack.item)
                        itemBlock is ShulkerBoxBlock && itemBlock.color == identifier.color
                    }
                    else -> false
                }
            }
            is MobIdentifier.TexturedHead -> {
                e is ArmorStand &&
                        e.hasItemInSlot(EquipmentSlot.HEAD) &&
                        e.getItemBySlot(EquipmentSlot.HEAD).getTexture() == identifier.texture
            }
            is MobIdentifier.TexturedItemDisplay -> {
                e is Display.ItemDisplay && e.itemStack.getTexture() == identifier.texture
            }
            is MobIdentifier.TexturedBlockDisplay -> {
                e is Display.BlockDisplay && e.blockState.block.name.string == identifier.texture
            }
            is MobIdentifier.PlayerSkin -> {
                e is Player && e.getSkinTexture() == identifier.texture
            }
            is MobIdentifier.SpecificTropicalFish -> {
                e is TropicalFish &&
                        e.pattern.name == identifier.pattern &&
                        e.baseColor.name == identifier.baseColor &&
                        e.patternColor.name == identifier.patternColor
            }
            is MobIdentifier.SpecificParrot -> {
                e is Parrot && e.variant == identifier.variant
            }
        }
    }

    fun shouldForceRender(): Boolean {
        return LocationAPI.island.value == SkyBlockIsland.SAFARI
    }

    init {
        on<WorldRenderEvent.Entity.Post> {
            val entity = this.entity ?: return@on

            if (`floorDrop$toggle` && entity is Display.ItemDisplay && entity.itemStack.`is`(Items.STRING)) {
                if (`floorDrop$only_in_biome`) {
                    val p = player ?: return@on
                    val mobBiome = getCritterSafariBiome(entity.x, entity.z)
                    val playerBiome = getCritterSafariBiome(p.x, p.z)
                    if (playerBiome != mobBiome) return@on
                }

                val pos = entity.blockPosition()
                val searchBox = AABB(pos)

                val stringDisplays = level?.getEntitiesOfClass(Display.ItemDisplay::class.java, searchBox) {
                    it.itemStack.`is`(Items.STRING) && it.blockPosition() == pos
                } ?: emptyList()

                if (stringDisplays.size >= 3) {
                    val minId = stringDisplays.minOfOrNull { it.id }
                    if (entity.id == minId) {
                        val blockBox = AABB(
                            pos.x.toDouble(), pos.y + 1.0, pos.z.toDouble(),
                            pos.x + 1.0, pos.y + 1.0, pos.z + 1.0
                        )
                        extractStyledBox(blockBox, `floorDrop$color`.rgb, depth = false)
                    }
                }
                return@on
            }

            val mobBiome = getCritterSafariBiome(entity.x, entity.z)
            if (mobBiome == SafariBiome.OUTSIDE) return@on

            val biomeConfig = safariRegistry[mobBiome] ?: return@on
            if (!biomeConfig.isEnabled()) return@on

            if (biomeConfig.isOnlyInBiome()) {
                val p = player ?: return@on
                val playerBiome = getCritterSafariBiome(p.x, p.z)
                if (playerBiome != mobBiome) return@on
            }

            val mob = biomeConfig.mobs.firstOrNull { isMatchingEntity(entity, it.identifier) } ?: return@on
            if (!mob.isEnabled()) return@on

            val renderBox = if (entity is Display) {
                val isShulkerDisplay = when (entity) {
                    is Display.BlockDisplay -> entity.blockState.block is ShulkerBoxBlock
                    is Display.ItemDisplay -> Block.byItem(entity.itemStack.item) is ShulkerBoxBlock
                    else -> false
                }

                if (isShulkerDisplay) {
                    AABB(entity.x - 0.5, entity.y, entity.z - 0.5, entity.x + 0.5, entity.y + 1.0, entity.z + 0.5)
                } else {
                    AABB(entity.x - 0.3, entity.y - 0.45, entity.z - 0.3, entity.x + 0.3, entity.y + 0.15, entity.z + 0.3)
                }
            } else if (entity is ArmorStand) {
                AABB(entity.x - 0.3, entity.y + 1.35, entity.z - 0.3, entity.x + 0.3, entity.y + 1.95, entity.z + 0.3)
            } else {
                entity.renderBoundingBox
            }

            extractFrameBox(renderBox, mob.color().rgb, 2f, false)
        }
    }
}