package foo.starred.nebulune.mixin.mixins.athen;

import foo.starred.athen.modules.impl.slayer.SlayerHighlight;
import foo.starred.nebulune.modules.impl.slayer.BossESP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SlayerHighlight.class)
public class SlayerHighlightMixin {
    @ModifyArg(method = "fn", at = @At(value = "INVOKE", target = "Lfoo/starred/athen/api/rendering/level/impl/extensions/impl/BoxExtensionsKt;extractFrameBox$default(Lnet/minecraft/world/phys/AABB;IFZILjava/lang/Object;)V"), index = 4)
    private int nebulune$r(int par5) {
        return BossESP.INSTANCE.getDepth() ? par5 : par5 & ~8;
    }
}