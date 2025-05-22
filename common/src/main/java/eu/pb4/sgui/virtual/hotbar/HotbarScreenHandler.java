package eu.pb4.sgui.virtual.hotbar;

import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.gui.HotbarGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import eu.pb4.sgui.virtual.inventory.VirtualSlot;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class HotbarScreenHandler extends VirtualScreenHandler {
    private final int x = 0;
    public NonNullList<ItemStack> slotsOld = null;

    public HotbarScreenHandler(@Nullable MenuType<?> type, int syncId, SlotGuiInterface gui, Player player) {
        super(type, syncId, gui, player);
    }

    @Override
    public HotbarGui getGui() {
        return (HotbarGui) super.getGui();
    }

    @Override
    protected void setupSlots(Player player) {
        for (int n = 0; n < this.getGui().getSize(); n++) {
            int nR = HotbarGui.VANILLA_TO_GUI_IDS[n];
            Slot slot = this.getGui().getSlotRedirect(nR);
            if (slot != null) {
                this.addSlot(slot);
            } else {
                this.addSlot(new VirtualSlot(this.getGui(), nR, 0, 0));
            }
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        /*try {
            this.getGui().onTick();
            if (this.getGui().isOpen()) {
                if (this.slotsOld == null) {
                    this.slotsOld = DefaultedList.ofSize(this.slots.size(), ItemStack.EMPTY);
                    for (int x = 0; x < HotbarGui.SIZE; x++) {
                        this.slotsOld.set(x, this.slots.get(x).getItem());
                    }
                } else {
                    for (int i = 0; i < this.slots.size(); i++) {
                        ItemStack itemStack = this.slots.get(i).getItem();

                        if (!ItemStack.areEqual(itemStack, this.slotsOld.get(i))) {
                            itemStack = itemStack.copy();
                            this.slotsOld.set(i, itemStack);
                            this.getGui().getPlayer().connection.send(new SetPlayerInventoryS2CPacket(i, itemStack));

                            /*if ((i > -1 && i < 5) || i == 45) {
                                GuiHelpers.sendSlotUpdate(this.getGui().getPlayer(), 0, i, itemStack);
                            } else {
                                int n = i;

                                if (i > 35 && i < 45) {
                                    n = i - 36;
                                } else if (i > 4 && i < 9) {
                                    n = i - 5;
                                }
                                GuiHelpers.sendSlotUpdate(this.getGui().getPlayer(), -2, n, itemStack);
                            }* /
                        }
                    }
                }
            }
        } catch (Exception e) {
            this.getGui().handleException(e);
        }*/
    }

    @ApiStatus.Internal
    public void syncSelectedSlot() {
        var gui = this.getGui();
        if (gui.isOpen()) {
            int index = gui.getHotbarSlotIndex(this.slots.size(), gui.getSelectedSlot());
            GuiHelpers.sendSlotUpdate(gui.getPlayer(), this.containerId, index, this.getSlot(index).getItem(), this.incrementStateId());
        }
    }

    @ApiStatus.Internal
    public void syncOffhandSlot() {
        var gui = this.getGui();
        if (gui.isOpen()) {
            int index = gui.getOffhandSlotIndex();
            GuiHelpers.sendSlotUpdate(gui.getPlayer(), this.containerId, index, this.getSlot(index).getItem(), this.incrementStateId());
        }
    }
}
