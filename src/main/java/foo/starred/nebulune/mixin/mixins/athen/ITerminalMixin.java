package foo.starred.nebulune.mixin.mixins.athen;

import foo.starred.athen.api.dungeon.terminals.TerminalAPI;
import foo.starred.athen.api.dungeon.terminals.TerminalType;
import foo.starred.athen.config.dsl.impl.builders.sound.ConfigSoundOption;
import foo.starred.athen.modules.impl.dungeon.terminals.simulator.TerminalSimulator;
import foo.starred.athen.modules.impl.dungeon.terminals.simulator.base.ITerminalSim;
import foo.starred.athen.modules.impl.dungeon.terminals.solver.TerminalSolver;
import foo.starred.athen.modules.impl.dungeon.terminals.solver.base.Click;
import foo.starred.athen.modules.impl.dungeon.terminals.solver.base.ITerminal;
import foo.starred.athen.utils.PlayerUtilsKt;
import foo.starred.nebulune.Nebulune;
import foo.starred.nebulune.accessors.ITerminalAccessor;
import foo.starred.nebulune.modules.impl.dungeons.AutoTerms;
import foo.starred.nebulune.modules.impl.dungeons.HoverTerms;
import foo.starred.nebulune.modules.impl.dungeons.QueueTerms;
import foo.starred.snowbird.api.ClientKt;
import kotlin.Unit;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Mixin(ITerminal.class)
public abstract class ITerminalMixin implements ITerminalAccessor {
    @Final
    @Shadow
    private CopyOnWriteArrayList<Click> list;

    @Final
    @Shadow
    private TerminalType terminalType;

    @Shadow
    public abstract TerminalType getTerminalType();

    @Shadow
    protected abstract int getInt0();

    @Shadow
    protected abstract int getInt1();

    @Shadow
    protected abstract float getFloat();

    @Shadow
    protected abstract Click forSlot(int slot);

    @Shadow
    protected abstract boolean valid(Click click);

    @Shadow
    public abstract void onResync();

    @Shadow
    protected abstract void compute(List<ItemStack> list);

    @Shadow
    private boolean clicked;

    @Override
    public CopyOnWriteArrayList<Click> nebulune$getList() {
        return list;
    }

    @Override
    public int nebulune$int0() {
        return getInt0();
    }

    @Override
    public int nebulune$int1() {
        return getInt1();
    }

    @Override
    public float nebulune$float() {
        return getFloat();
    }

    @Inject(method = "onOpen", at = @At("HEAD"))
    private void nebulune$onOpen(CallbackInfo ci) {
        QueueTerms.INSTANCE.setYearning(false);
        HoverTerms.INSTANCE.reset();
    }

    @Inject(method = "onClose", at = @At("HEAD"))
    private void nebulune$onClose(CallbackInfo ci) {
        QueueTerms.INSTANCE.getClicks().clear();
        HoverTerms.INSTANCE.reset();
    }

    @Inject(method = "click*", at = @At(value = "INVOKE", target = "Lfoo/starred/athen/modules/impl/dungeon/terminals/solver/base/ITerminal;forSlot(I)Lfoo/starred/athen/modules/impl/dungeon/terminals/solver/base/Click;", shift = At.Shift.AFTER), cancellable = true)
    private void nebulune$click(float mx, float my, float width, float height, int mouseButton, CallbackInfo ci) {
        if (!QueueTerms.INSTANCE.getEnabled()) return;

        float sp = getFloat();
        float pad = TerminalSolver.INSTANCE.getUi$padding();
        int slots = getTerminalType().getSlots();
        float gridW = getInt0() * sp + 2 * pad;
        float gridH = ((float) slots / 9 - 2) * sp + 2 * pad;
        float headerH = TerminalSolver.INSTANCE.getUi$hideHeader() ? 0f : 20f;
        float padding = TerminalSolver.INSTANCE.getUi$hideHeader() ? 0f : 6f;

        float ox = width / 2 - gridW / 2;
        float oy = height / 2 - (gridH + headerH + padding) / 2;

        int x = (int) ((mx - ox - pad) / sp) + getInt1();
        int y = (int) ((my - (oy + headerH + padding) - pad) / sp) + 1;
        if (x < getInt1() || x >= getInt1() + getInt0() || y < 1) return;

        int slot = x + y * 9;
        if (slot >= slots) return;

        Click c = forSlot(slot);
        if (c == null) return;
        if (c.getButton() != mouseButton && !(terminalType == TerminalType.RUBIX && TerminalSolver.INSTANCE.getRubix$left())) return;

        nebulune$adjust(c);
        ci.cancel();

        if (QueueTerms.INSTANCE.getYearning()) {
            QueueTerms.INSTANCE.getClicks().add(c);
            return;
        }

        nebulune$clickClick(c);
    }

    @ModifyVariable(method = "main(Lnet/minecraft/client/gui/GuiGraphicsExtractor;FFFFFLorg/joml/Matrix3x2f;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)V", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private String nebulune$modifyTitleText(String titleText) {
        if (!QueueTerms.INSTANCE.getEnabled()) return titleText;
        return titleText + " - " + QueueTerms.INSTANCE.getClicks().size() + QueueTerms.INSTANCE.getClicks().size();
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lfoo/starred/athen/modules/impl/dungeon/terminals/solver/base/ITerminal;compute(Ljava/util/List;)V", shift = At.Shift.AFTER))
    private void nebulune$update(List<ItemStack> items, CallbackInfo ci) {
        QueueTerms.INSTANCE.setYearning(false);
        AutoTerms.onUpdate();

        if (!QueueTerms.INSTANCE.getEnabled()) return;
        if (QueueTerms.INSTANCE.getClicks().isEmpty()) return;

        Click next = QueueTerms.INSTANCE.getClicks().getFirst();
        if (!valid(next)) {
            QueueTerms.INSTANCE.getClicks().clear();
            return;
        }

        for (Click c : QueueTerms.INSTANCE.getClicks()) nebulune$adjust(c);
        QueueTerms.INSTANCE.getClicks().removeFirst();
        nebulune$clickClick(next);
    }

    @Unique
    private void nebulune$clickClick(Click click) {
        QueueTerms.INSTANCE.setYearning(true);
        ConfigSoundOption sound = TerminalSolver.INSTANCE.getClickSound();

        if (TerminalSimulator.INSTANCE.getS().getValue()) {
            var client = ClientKt.getClient();
            //~ if >= 26.2 'client.screen' -> 'client.gui.screen()'
            var screen = client.screen;
            if (!(screen instanceof ITerminalSim sim)) return;

            var slots = sim.getMenu().slots;
            int slotIndex = click.getSlot();
            if (slotIndex >= slots.size()) return;

            var slot = slots.get(slotIndex);
            sim.slotClicked(slot, slotIndex, click.getButton(), click.getButton() == 0 ? ContainerInput.CLONE : ContainerInput.PICKUP);
            TerminalSolver.INSTANCE.setLast(System.currentTimeMillis());
            clicked = true;

            if (sound.getEnabled()) sound.play(sound.getVolume(), sound.getPitch());
            return;
        }

        if (sound.getEnabled()) sound.play(sound.getVolume(), sound.getPitch());

        PlayerUtilsKt.guiClick(
                TerminalAPI.INSTANCE.getId(),
                click.getSlot(),
                click.getButton() == 0 ? 2 : click.getButton(),
                click.getButton() == 0 ? ContainerInput.CLONE : ContainerInput.PICKUP
        );
        TerminalSolver.INSTANCE.setLast(System.currentTimeMillis());
        clicked = true;

        int id = TerminalAPI.INSTANCE.getId();
        int timeout = QueueTerms.INSTANCE.getTimeout();

        Nebulune.afterTimed(timeout, () -> {
            if (!TerminalAPI.INSTANCE.getOpened().getValue()) return Unit.INSTANCE;
            if (id != TerminalAPI.INSTANCE.getId()) return Unit.INSTANCE;

            //~ if >= 26.2 'ClientKt.getClient().screen' -> 'ClientKt.getClient().gui.screen()'
            var menu = ClientKt.getClient().screen;
            if (!(menu instanceof AbstractContainerScreen<?> a)) return Unit.INSTANCE;
            var items = a.getMenu().getItems().subList(0, terminalType.getSlots());

            QueueTerms.INSTANCE.getClicks().clear();
            compute(items);
            QueueTerms.INSTANCE.setYearning(false);

            onResync();
            return Unit.INSTANCE;
        });
    }

    @Unique
    private void nebulune$adjust(Click click) {
        TerminalType type = getTerminalType();

        if (type == TerminalType.NUMBERS || type == TerminalType.PANES || type == TerminalType.NAME || type == TerminalType.COLORS) {
            list.remove(click);
            return;
        }

        if (type == TerminalType.RUBIX) {
            int index = -1;

            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getSlot() == click.getSlot()) {
                    index = i;
                    break;
                }
            }

            if (index == -1) return;

            int next = list.get(index).getButton() + (click.getButton() == 0 ? -1 : 1);
            if (next == 0) list.remove(index);
            else list.set(index, new Click(click.getSlot(), next));
        }
    }
}