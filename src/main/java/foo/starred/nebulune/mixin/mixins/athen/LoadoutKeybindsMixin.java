package foo.starred.nebulune.mixin.mixins.athen;

import com.mojang.blaze3d.platform.InputConstants;
import foo.starred.athen.events.core.AthenEvent;
import foo.starred.athen.modules.impl.general.LoadoutKeybinds;
import foo.starred.kbus.data.event.traits.KBusCancellableTrait;
import foo.starred.nebulune.modules.impl.general.LoadoutHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LoadoutKeybinds.class, remap = false)
public class LoadoutKeybindsMixin {
    @Inject(method = "fn", at = @At("TAIL"))
    private void nebulune$fn(KBusCancellableTrait $this$fn, InputConstants.Key key, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && LoadoutHelper.INSTANCE.getAutoClose()) LoadoutHelper.close();
    }
}