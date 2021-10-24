package eu.pb4.sgui.virtual.hotbar;

import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.gui.HotbarGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import eu.pb4.sgui.virtual.inventory.VirtualSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

public class HotbarScreenHandler extends VirtualScreenHandler {
    private final int x = 0;
    public DefaultedList<ItemStack> slotsOld = null;

    public HotbarScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId, SlotGuiInterface gui, PlayerEntity player) {
        super(type, syncId, gui, player);
    }

    @Override
    public HotbarGui getGui() {
        return (HotbarGui) super.getGui();
    }

    @Override
    protected void setupSlots(PlayerEntity player) {
        for (int n = 0; n < this.getGui().getSize(); n++) {
            int nR = HotbarGui.VANILLA_TO_GUI_IDS[n];
            Slot slot = this.getGui().getSlotRedirect(nR);
            if (slot != null) {
                this.addSlot(slot);
            } else {
                this.addSlot(new VirtualSlot(inventory, nR, 0, 0));
            }
        }
    }

    @Override
    public void sendContentUpdates() {
        try {
            this.getGui().onTick();

            if (this.getGui().isOpen()) {
                if (this.slotsOld == null) {
                    this.slotsOld = DefaultedList.ofSize(this.slots.size(), ItemStack.EMPTY);
                    for (int x = 0; x < HotbarGui.SIZE; x++) {
                        this.slotsOld.set(x, this.slots.get(x).getStack());
                    }
                } else {
                    for (int i = 0; i < this.slots.size(); i++) {
                        ItemStack itemStack = this.slots.get(i).getStack();

                        if (!ItemStack.areEqual(itemStack, this.slotsOld.get(i))) {
                            this.slotsOld.set(i, itemStack.copy());

                            if ((i > -1 && i < 5) || i == 45) {
                                GuiHelpers.sendSlotUpdate(this.getGui().getPlayer(), 0, i, itemStack);
                            } else {
                                int n = i;

                                if (i > 35 && i < 45) {
                                    n = i - 36;
                                } else if (i > 4 && i < 9) {
                                    n = i - 5;
                                }
                                GuiHelpers.sendSlotUpdate(this.getGui().getPlayer(), -2, n, itemStack);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
