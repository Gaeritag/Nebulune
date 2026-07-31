package foo.starred.nebulune.mixin.mixins.athen;

import foo.starred.athen.modules.impl.render.highlight.MobHighlight;
import foo.starred.nebulune.modules.impl.render.MobHighlightESP;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static foo.starred.athen.api.rendering.level.impl.extensions.impl.BoxExtensionsKt.extractFrameBox;
import static foo.starred.nebulune.utils.Render3DKt.extractTracer;

@Mixin(MobHighlight.class)
public class MobHighlightMixin {
    @Inject(method = "fn1", at = @At(value = "INVOKE", target = "Lfoo/starred/athen/api/rendering/level/impl/extensions/impl/BoxExtensionsKt;extractFrameBox$default(Lnet/minecraft/world/phys/AABB;IFZILjava/lang/Object;)V"), cancellable = true)
    private void nebulune$fn1(AABB aabb, int color, CallbackInfo ci) {
        ci.cancel();

        extractFrameBox(aabb, color, 2f, MobHighlightESP.INSTANCE.getDepth());
        if (MobHighlightESP.INSTANCE.getTracer()) extractTracer(new Vec3(aabb.minX, aabb.minY, aabb.minZ), color, 3f, MobHighlightESP.INSTANCE.getDepth());
    }
}
