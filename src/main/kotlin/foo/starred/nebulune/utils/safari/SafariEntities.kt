package foo.starred.nebulune.utils.safari

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.parrot.Parrot
import net.minecraft.world.item.DyeColor
import java.awt.Color

sealed class MobIdentifier {
    data class VanillaEntity(val type: EntityType<*>) : MobIdentifier()
    data class ColoredShulker(val color: DyeColor) : MobIdentifier()
    data class TexturedHead(val texture: String) : MobIdentifier()
    data class TexturedItemDisplay(val texture: String) : MobIdentifier()
    data class TexturedBlockDisplay(val texture: String) : MobIdentifier()
    data class PlayerSkin(val texture: String) : MobIdentifier()
    data class SpecificTropicalFish(val pattern: String, val baseColor: String, val patternColor: String) : MobIdentifier()
    data class SpecificParrot(val variant: Parrot.Variant) : MobIdentifier()
}

data class SafariMob(
    val identifier: MobIdentifier,
    val isEnabled: () -> Boolean,
    val color: () -> Color
)