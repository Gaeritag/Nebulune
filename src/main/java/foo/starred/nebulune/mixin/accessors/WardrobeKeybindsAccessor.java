package foo.starred.nebulune.mixin.accessors;

import foo.starred.athen.modules.impl.general.WardrobeKeybinds;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = WardrobeKeybinds.class, remap = false)
public interface WardrobeKeybindsAccessor {
    @Accessor("slots")
    List<Object> nebulune$getSlots();
}