package eu.pb4.sgui.api.containerwrappers;

import eu.pb4.sgui.api.gui.GuiLike;
import eu.pb4.sgui.api.containerwrappers.slot.WrappingSlot;
import eu.pb4.sgui.api.gui.SlotBasedGui;
import eu.pb4.sgui.impl.FauxHashedStack;
import eu.pb4.sgui.mixin.ScreenHandlerAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public abstract class AbstractWrapperMenu extends AbstractContainerMenu {
    private final GuiLike gui;

    protected AbstractWrapperMenu(@Nullable MenuType<?> menuType, int containerId, GuiLike guiInterface) {
        super(menuType, containerId);
        this.gui = guiInterface;
    }

    public GuiLike getBackingGui() {
        return this.gui;
    }

    @Override
    public void addSlotListener(ContainerListener listener) {
        super.addSlotListener(listener);
        this.gui.afterOpen();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.gui.stillValid();
    }


    @Override
    public void broadcastChanges() {
        try {
            this.gui.onTick();
        } catch (Exception e) {
            this.gui.handleException(e);
        }
        super.broadcastChanges();
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return !(slot instanceof WrappingSlot) && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return this.gui.onButtonClick(id);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        try {
            this.gui.onRemoved();
        } catch (Throwable e) {
            this.gui.handleException(e);
        }
    }

    public void postRemoved(ServerPlayer serverPlayer) {
        try {
            this.gui.afterRemoval();
        } catch (Throwable e) {
            this.gui.handleException(e);
        }
    }
}
