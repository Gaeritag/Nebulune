package foo.starred.nebulune.mixin.mixins.athen;

import foo.starred.athen.modules.impl.slayer.SlayerHighlight;
import foo.starred.nebulune.modules.impl.slayer.BossESP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SlayerHighlight.class)
public class SlayerHighlightMixin {
    @ModifyArg(method = "fn", at = @At(value = "INVOKE", target = "Lfoo/starred/parallax/api/primitives/ParallaxBox;frame$default(Lfoo/starred/parallax/api/primitives/ParallaxBox;Lnet/minecraft/world/phys/AABB;IFZILjava/lang/Object;)V"), index = 4)
    private boolean nebulune$r(boolean par5) {
        return BossESP.INSTANCE.getDepth();
    }
}