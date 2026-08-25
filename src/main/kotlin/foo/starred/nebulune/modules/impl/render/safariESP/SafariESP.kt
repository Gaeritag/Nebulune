package foo.starred.nebulune.modules.impl.render.safariESP

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import kotlin.Triple
import foo.starred.athen.api.rendering.level.impl.extensions.impl.extractFrameBox
import foo.starred.athen.config.Category
import foo.starred.athen.events.TickEvent
import foo.starred.athen.events.WorldRenderEvent
import foo.starred.athen.modules.Module
import foo.starred.athen.utils.render.renderBoundingBox
import foo.starred.nebulune.utils.safari.MobIdentifier
import foo.starred.nebulune.utils.safari.SafariBiome
import foo.starred.nebulune.utils.safari.SafariMob
import foo.starred.nebulune.utils.safari.getCritterSafariBiome
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.renderer.entity.state.ShulkerRenderState
import net.minecraft.world.entity.Entity
//? if >= 26.2
//import net.minecraft.world.entity.EntityTypes as EntityType
//? if = 26.1
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.item.DyeColor
import tech.thatgravyboat.skyblockapi.utils.extentions.getTexture
import java.awt.Color

@Load
// @OnlyIn(islands = [SkyBlockIsland.SAFARI])
object SafariESP : Module(
    "Safari ESP",
    "ESP for Safari mobs",
    Category.RENDER
) {
    // CAVERN
    private val cavernGroup by config.group("Cavern Biome")
    val `cavern$toggle` by cavernGroup.switch("Toggle cavern")
    val `cavern$only_in_biome` by cavernGroup.switch("Toggle cavern mobs only in cavern biome")
    val `cavern$cavernrnfish` by cavernGroup.switch("cavernfish")
    val `cavern$cavernrnfish_color` by cavernGroup.colorPicker("Color", Color(90, 40, 20))
    val `cavern$flitter` by cavernGroup.switch("Flitter")
    val `cavern$flitter_color` by cavernGroup.colorPicker("Color", Color(80, 80, 100))
    val `cavern$shyworm` by cavernGroup.switch("Shyworm")
    val `cavern$shyworm_color` by cavernGroup.colorPicker("Color", Color(100, 200, 100))
    val `cavern$driftling` by cavernGroup.switch("Driftling")
    val `cavern$driftling_color` by cavernGroup.colorPicker("Color", Color(255, 150, 50))
    val `cavern$chuckwalla` by cavernGroup.switch("Chuckwalla")
    val `cavern$chuckwalla_color` by cavernGroup.colorPicker("Color", Color(120, 110, 100))
    val `cavern$rockmite` by cavernGroup.switch("Rockmite")
    val `cavern$rockmite_color` by cavernGroup.colorPicker("Color", Color(180, 180, 180))
    val `cavern$scrappy` by cavernGroup.switch("Scrappy")
    val `cavern$scrappy_color` by cavernGroup.colorPicker("Color", Color(200, 120, 120))
    val `cavern$snoozle` by cavernGroup.switch("Snoozle")
    val `cavern$snoozle_color` by cavernGroup.colorPicker("Color", Color(200, 50, 50))
    val `cavern$gemzie` by cavernGroup.switch("Gemzie")
    val `cavern$gemzie_color` by cavernGroup.colorPicker("Color", Color(150, 200, 255))

    // FOREST
    private val forestGroup by config.group("Forest Biome")
    val `forest$toggle` by forestGroup.switch("Toggle Forest")
    val `forest$only_in_biome` by forestGroup.switch("Toggle forest mobs only in forest biome")
    val `forest$foxtrot` by forestGroup.switch("Foxtrot")
    val `forest$foxtrot_color` by forestGroup.colorPicker("Color", Color(255, 150, 50))
    val `forest$bluebird` by forestGroup.switch("Bluebird")
    val `forest$bluebird_color` by forestGroup.colorPicker("Color", Color(50, 50, 255))
    val `forest$honeybug` by forestGroup.switch("Honeybug")
    val `forest$honeybug_color` by forestGroup.colorPicker("Color", Color(255, 200, 50))
    val `forest$treefrog` by forestGroup.switch("Treefrog")
    val `forest$treefrog_color` by forestGroup.colorPicker("Color", Color(100, 200, 100))
    val `forest$woodchucker` by forestGroup.switch("Woodchucker")
    val `forest$woodchucker_color` by forestGroup.colorPicker("Color", Color(80, 60, 40))
    val `forest$fluffling` by forestGroup.switch("Fluffling")
    val `forest$fluffling_color` by forestGroup.colorPicker("Color", Color(255, 255, 255))
    val `forest$hideonfloor` by forestGroup.switch("Hideonfloor")
    val `forest$hideonfloor_color` by forestGroup.colorPicker("Color", Color(100, 255, 100))
    val `forest$parakeet` by forestGroup.switch("Parakeet")
    val `forest$parakeet_color` by forestGroup.colorPicker("Color", Color(50, 255, 50))
    val `forest$macaw` by forestGroup.switch("Macaw")
    val `forest$macaw_color` by forestGroup.colorPicker("Color", Color(255, 50, 50))

    // HAUNTED
    private val hauntedGroup by config.group("Haunted Biome")
    val `haunted$toggle` by hauntedGroup.switch("Toggle Haunted")
    val `haunted$only_in_biome` by hauntedGroup.switch("Toggle haunted mobs only in haunted biome")
    val `haunted$areita` by hauntedGroup.switch("Areita")
    val `haunted$areita_color` by hauntedGroup.colorPicker("Color", Color(50, 150, 150))
    val `haunted$bloodbat` by hauntedGroup.switch("Bloodbat")
    val `haunted$bloodbat_color` by hauntedGroup.colorPicker("Color", Color(100, 200, 100))
    val `haunted$duplico` by hauntedGroup.switch("Duplico")
    val `haunted$duplico_color` by hauntedGroup.colorPicker("Color", Color(255, 100, 100))
    val `haunted$gazer` by hauntedGroup.switch("Gazer")
    val `haunted$gazer_color` by hauntedGroup.colorPicker("Color", Color(50, 255, 120))
    val `haunted$litterbug` by hauntedGroup.switch("Litterbug")
    val `haunted$litterbug_color` by hauntedGroup.colorPicker("Color", Color(150, 50, 200))
    val `haunted$solsnatcher` by hauntedGroup.switch("Solsnatcher")
    val `haunted$solsnatcher_color` by hauntedGroup.colorPicker("Color", Color(50, 50, 150))
    val `haunted$gimmiegold` by hauntedGroup.switch("Gimmiegold")
    val `haunted$gimmiegold_color` by hauntedGroup.colorPicker("Color", Color(255, 200, 50))
    val `haunted$hideonwall` by hauntedGroup.switch("Hideonwall")
    val `haunted$hideonwall_color` by hauntedGroup.colorPicker("Color", Color(150, 50, 200))
    val `haunted$hideyho` by hauntedGroup.switch("Hideyho")
    val `haunted$hideyho_color` by hauntedGroup.colorPicker("Color", Color(180, 180, 180))
    val `haunted$doomspiral` by hauntedGroup.switch("Doomspiral")
    val `haunted$doomspiral_color` by hauntedGroup.colorPicker("Color", Color(50, 100, 100))

    // ICY
    private val icyGroup by config.group("Icy Biome")
    val `icy$toggle` by icyGroup.switch("Toggle Icy")
    val `icy$only_in_biome` by icyGroup.switch("Toggle icy mobs only in icy biome")
    val `icy$strongarm` by icyGroup.switch("Strongarm")
    val `icy$strongarm_color` by icyGroup.colorPicker("Color", Color(255, 255, 255))
    val `icy$tepid` by icyGroup.switch("Tepid")
    val `icy$tepid_color` by icyGroup.colorPicker("Color", Color(150, 150, 150))
    val `icy$polaris` by icyGroup.switch("Polaris")
    val `icy$polaris_color` by icyGroup.colorPicker("Color", Color(75, 75, 75))
    val `icy$shuddersquid` by icyGroup.switch("Shuddersquid")
    val `icy$shuddersquid_color` by icyGroup.colorPicker("Color", Color(100, 255, 200))
    val `icy$billygoat` by icyGroup.switch("Billygoat")
    val `icy$billygoat_color` by icyGroup.colorPicker("Color", Color(50, 50, 50))
    val `icy$mantis_shrimp` by icyGroup.switch("Mantis Shrimp")
    val `icy$mantis_shrimp_color` by icyGroup.colorPicker("Color", Color(50, 150, 150))
    val `icy$nozzlenose` by icyGroup.switch("Nozzlenose")
    val `icy$nozzlenose_color` by icyGroup.colorPicker("Color", Color(150, 200, 255))
    val `icy$troodon` by icyGroup.switch("Troodon")
    val `icy$troodon_color` by icyGroup.colorPicker("Color", Color(100, 100, 200))
    val `icy$wumpa` by icyGroup.switch("Wumpa")
    val `icy$wumpa_color` by icyGroup.colorPicker("Color", Color(100, 100, 100))

    private val safariRegistry: Map<SafariBiome, Triple<() -> Boolean, () -> Boolean, List<SafariMob>>> = mapOf(
        SafariBiome.CAVERN to Triple({ `cavern$toggle` }, { `cavern$only_in_biome` }, listOf(
            SafariMob(MobIdentifier.VanillaEntity(EntityType.TROPICAL_FISH), { `cavern$cavernrnfish` }, { `cavern$cavernrnfish_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.BAT), { `cavern$flitter` }, { `cavern$flitter_color` }),
            SafariMob(MobIdentifier.TexturedHead("BASE64_ICI"), { `cavern$shyworm` }, { `cavern$shyworm_color` }),
            SafariMob(MobIdentifier.TexturedHead("BASE64_ICI"), { `cavern$driftling` }, { `cavern$driftling_color` }),
            SafariMob(MobIdentifier.TexturedHead("BASE64_ICI"), { `cavern$chuckwalla` }, { `cavern$chuckwalla_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.SILVERFISH), { `cavern$rockmite` }, { `cavern$rockmite_color` }),
            SafariMob(MobIdentifier.TexturedHead("BASE64_ICI"), { `cavern$scrappy` }, { `cavern$scrappy_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.SNIFFER), { `cavern$snoozle` }, { `cavern$snoozle_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.VEX), { `cavern$gemzie` }, { `cavern$gemzie_color` })
        )),
        SafariBiome.FOREST to Triple({ `forest$toggle` }, { `forest$only_in_biome` }, listOf(
            SafariMob(MobIdentifier.VanillaEntity(EntityType.FOX), { `forest$foxtrot` }, { `forest$foxtrot_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.PARROT), { `forest$bluebird` }, { `forest$bluebird_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.BEE), { `forest$honeybug` }, { `forest$honeybug_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.FROG), { `forest$treefrog` }, { `forest$treefrog_color` }),
            SafariMob(MobIdentifier.TexturedHead("BASE64_ICI"), { `forest$woodchucker` }, { `forest$woodchucker_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.PANDA), { `forest$fluffling` }, { `forest$fluffling_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.SLIME), { `forest$hideonfloor` }, { `forest$hideonfloor_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.PARROT), { `forest$parakeet` }, { `forest$parakeet_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.PARROT), { `forest$macaw` }, { `forest$macaw_color` })
        )),
        SafariBiome.HAUNTED to Triple({ `haunted$toggle` }, { `haunted$only_in_biome` }, listOf(
            SafariMob(MobIdentifier.VanillaEntity(EntityType.CAVE_SPIDER), { `haunted$areita` }, { `haunted$areita_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.BAT), { `haunted$bloodbat` }, { `haunted$bloodbat_color` }),
            SafariMob(MobIdentifier.TexturedHead("BASE64_ICI"), { `haunted$duplico` }, { `haunted$duplico_color` }),
            SafariMob(MobIdentifier.TexturedHead("BASE64_ICI"), { `haunted$gazer` }, { `haunted$gazer_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.ENDERMITE), { `haunted$litterbug` }, { `haunted$litterbug_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.PHANTOM), { `haunted$solsnatcher` }, { `haunted$solsnatcher_color` }),
            SafariMob(MobIdentifier.TexturedHead("BASE64_ICI"), { `haunted$gimmiegold` }, { `haunted$gimmiegold_color` }),
            SafariMob(MobIdentifier.ColoredShulker(DyeColor.PURPLE), { `haunted$hideonwall` }, { `haunted$hideonwall_color` }),
            SafariMob(MobIdentifier.PlayerSkin("BASE64_ICI"), { `haunted$hideyho` }, { `haunted$hideyho_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.WARDEN), { `haunted$doomspiral` }, { `haunted$doomspiral_color` })
        )),
        SafariBiome.ICY to Triple({ `icy$toggle` }, { `icy$only_in_biome` }, listOf(
            SafariMob(MobIdentifier.VanillaEntity(EntityType.SNOW_GOLEM), { `icy$strongarm` }, { `icy$strongarm_color` }),
            SafariMob(MobIdentifier.TexturedHead("BASE64_ICI"), { `icy$tepid` }, { `icy$tepid_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.POLAR_BEAR), { `icy$polaris` }, { `icy$polaris_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.GLOW_SQUID), { `icy$shuddersquid` }, { `icy$shuddersquid_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.GOAT), { `icy$billygoat` }, { `icy$billygoat_color` }),
            SafariMob(MobIdentifier.TexturedHead("BASE64_ICI"), { `icy$mantis_shrimp` }, { `icy$mantis_shrimp_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.DOLPHIN), { `icy$nozzlenose` }, { `icy$nozzlenose_color` }),
            SafariMob(MobIdentifier.TexturedHead("BASE64_ICI"), { `icy$troodon` }, { `icy$troodon_color` }),
            SafariMob(MobIdentifier.VanillaEntity(EntityType.RAVAGER), { `icy$wumpa` }, { `icy$wumpa_color` })
        ))
    )

    var ticks: Int = 200

    fun shouldForceRender(e: Entity): Boolean {
        val biome = getCritterSafariBiome(e.x, e.z)
        if (biome == SafariBiome.OUTSIDE) return false

        val biomeConfig = safariRegistry[biome] ?: return false
        if (!biomeConfig.first()) return false

        if (biomeConfig.second()) {
            val player = net.minecraft.client.Minecraft.getInstance().player ?: return false
            val playerBiome = getCritterSafariBiome(player.x, player.z)
            if (playerBiome != biome) return false
        }

        for (mobSetting in biomeConfig.third) {
            if (!mobSetting.isEnabled()) continue
            val isMatch = when (val id = mobSetting.identifier) {
                is MobIdentifier.VanillaEntity -> {
                    e.type == id.type
                }
                is MobIdentifier.ColoredShulker -> {
                    e.type == EntityType.SHULKER
                }
                is MobIdentifier.TexturedHead -> {
                    if (e is ArmorStand && e.hasItemInSlot(EquipmentSlot.HEAD)) {
                        val headTexture = e.getItemBySlot(EquipmentSlot.HEAD).getTexture()
                        headTexture == id.texture
                    } else {
                        false
                    }
                }
                is MobIdentifier.PlayerSkin -> {
                    if (e is AbstractClientPlayer) {
                        val textureProperty = e.gameProfile.properties.get("textures").firstOrNull()?.value
                        textureProperty == id.texture
                    } else {
                        false
                    }
                }
            }
            if (isMatch) return true
        }
        return false
    }

    init {
        on<WorldRenderEvent.Entity.Post> {
            val e = entity ?: return@on

            val mobBiome = getCritterSafariBiome(e.x, e.z)
            if (mobBiome == SafariBiome.OUTSIDE) return@on

            val biomeConfig = safariRegistry[mobBiome] ?: return@on
            val isBiomeEnabled = biomeConfig.first
            val isOnlyInBiome = biomeConfig.second
            val mobsInBiome = biomeConfig.third

            if (!isBiomeEnabled()) return@on

            if (isOnlyInBiome()) {
                val player = net.minecraft.client.Minecraft.getInstance().player ?: return@on
                val playerBiome = getCritterSafariBiome(player.x, player.z)

                if (playerBiome != mobBiome) return@on
            }

            for ((id, isEnabled, color) in mobsInBiome) {
                if (!isEnabled()) continue

                val isMatch = when (id) {
                    is MobIdentifier.VanillaEntity -> {
                        e.type == id.type
                    }
                    is MobIdentifier.ColoredShulker -> {
                        val r = renderState as? ShulkerRenderState
                        r != null && r.color == id.color
                    }
                    is MobIdentifier.TexturedHead -> {
                        if (e is ArmorStand && e.hasItemInSlot(EquipmentSlot.HEAD)) {
                            val headTexture = e.getItemBySlot(EquipmentSlot.HEAD).getTexture()
                            headTexture == id.texture
                        } else {
                            false
                        }
                    }
                    is MobIdentifier.PlayerSkin -> {
                        if (e is AbstractClientPlayer) {
                            val textureProperty = e.gameProfile.properties.get("textures").firstOrNull()?.value
                            textureProperty == id.texture
                        } else {
                            false
                        }
                    }
                }
                if (isMatch) {
                    extractFrameBox(e.renderBoundingBox, color().rgb, 2f, false)
                    break
                }
            }
        }
    }
}
