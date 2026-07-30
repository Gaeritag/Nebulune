package xyz.aerii.nebulune.mixin.mixins.athen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.aerii.athen.modules.impl.slayer.SlayerHighlight;
import xyz.aerii.nebulune.modules.impl.slayer.BossESP;

@Mixin(SlayerHighlight.class)
public class SlayerHighlightMixin {
    @ModifyArg(method = "fn", at = @At(value = "INVOKE", target = "Lxyz/aerii/athen/api/rendering/level/impl/extensions/impl/BoxExtensionsKt;extractFrameBox$default(Lnet/minecraft/world/phys/AABB;IFZILjava/lang/Object;)V"), index = 4)
    private int nebulune$r(int par5) {
        return BossESP.INSTANCE.getDepth() ? par5 : par5 & ~8;
    }
}