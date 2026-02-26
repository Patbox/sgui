package eu.pb4.sgui.api.gui.layered;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;

class BackendSimpleGui extends SimpleGui {
    public final LayeredGui gui;

    public BackendSimpleGui(MenuType<?> type, ServerPlayer player, boolean manipulatePlayerSlots, LayeredGui gui) {
        super(type, player, manipulatePlayerSlots);
        this.gui = gui;
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, ContainerInput action) {
        return this.gui.onAnyClick(index, type, action);
    }
    @Override
    public boolean onClick(int index, ClickType type, ContainerInput action, GuiElement element) {
        return this.gui.onClick(index, type, action, element);
    }

    @Override
    public void onClose() {
        this.gui.onClose();
    }

    @Override
    public void onPlayerClose(boolean b) {
        this.gui.onPlayerClose(false);
    }

    @Override
    public void onScreenHandlerClosed() {
        this.gui.onScreenHandlerClosed();
    }

    @Override
    public void onTick() {
        this.gui.onTick();
    }

    @Override
    public void onOpen() {
        this.gui.onOpen();
    }

    @Override
    public void beforeOpen() {
        this.gui.beforeOpen();
    }

    @Override
    public void afterOpen() {
        this.gui.afterOpen();
    }

    @Override
    public boolean canPlayerClose() {
        return this.gui.canPlayerClose();
    }

    @Override
    public boolean resetMousePosition() {
        return this.gui.resetMousePosition();
    }
}
