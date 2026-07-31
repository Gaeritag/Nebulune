package foo.starred.nebulune.mixin.mixins.athen;

import foo.starred.athen.modules.impl.kuudra.StunHelper;
import foo.starred.nebulune.modules.impl.kuudra.Stunner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StunHelper.class)
public class StunHelperMixin {
    @Inject(method = "fn", at = @At("HEAD"))
    private void nebulune$fn(CallbackInfo ci) {
        Stunner.fn();
    }
}
