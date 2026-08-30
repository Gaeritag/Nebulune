package foo.starred.nebulune.mixin.mixins.compat.entityculling;

import dev.tr7zw.entityculling.EntityCullingModBase;
import foo.starred.nebulune.modules.impl.render.SafariESP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityCullingModBase.class, remap = false)
public class EntityCullingModBaseMixin {

    @Unique
    private boolean isOverriding = false;

    @Unique
    private boolean originalSkipEntity = false;

    @Unique
    private boolean originalSkipBlock = false;

    @Inject(method = "clientTick", at = @At("HEAD"))
    private void disableTickCulling(CallbackInfo ci) {
        EntityCullingModBase instance = (EntityCullingModBase) (Object) this;
        if (instance.config == null) return;

        boolean forceRender = SafariESP.INSTANCE.shouldForceRender();

        if (forceRender && !isOverriding) {
            originalSkipEntity = instance.config.skipEntityCulling;
            originalSkipBlock = instance.config.skipBlockEntityCulling;

            instance.config.skipEntityCulling = true;
            instance.config.skipBlockEntityCulling = true;

            isOverriding = true;
        }
        else if (!forceRender && isOverriding) {
            instance.config.skipEntityCulling = originalSkipEntity;
            instance.config.skipBlockEntityCulling = originalSkipBlock;

            isOverriding = false;
        }
    }
}