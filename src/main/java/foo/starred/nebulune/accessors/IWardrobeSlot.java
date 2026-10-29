package foo.starred.nebulune.accessors;

import com.mojang.blaze3d.platform.InputConstants;

public interface IWardrobeSlot {
    int getIndex();
    default int getIdx() { return getIndex(); }
    InputConstants.Key getValue();
    boolean getEquipped();
}