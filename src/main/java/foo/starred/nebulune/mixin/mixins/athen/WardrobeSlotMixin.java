package foo.starred.nebulune.mixin.mixins.athen;

import foo.starred.nebulune.accessors.IWardrobeSlot;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "foo.starred.athen.modules.impl.general.WardrobeKeybinds$WardrobeSlot", remap = false)
public interface WardrobeSlotMixin extends IWardrobeSlot {
}