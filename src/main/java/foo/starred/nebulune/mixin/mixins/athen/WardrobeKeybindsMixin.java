package foo.starred.nebulune.mixin.mixins.athen;

import foo.starred.athen.events.core.CancellableEvent;
import foo.starred.athen.modules.impl.general.WardrobeKeybinds;
import foo.starred.nebulune.modules.impl.general.WardrobeHelper;
import foo.starred.snowbird.api.ClientKt;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WardrobeKeybinds.class)
public class WardrobeKeybindsMixin {
    @Inject(method = "fn", at = @At("TAIL"))
    private void nebulune$fn(CancellableEvent $this$fn, int key, CallbackInfo ci) {
        LocalPlayer player = ClientKt.getClient().player;
        if (player != null && WardrobeHelper.INSTANCE.getAutoClose()) WardrobeHelper.close(1);
    }
}