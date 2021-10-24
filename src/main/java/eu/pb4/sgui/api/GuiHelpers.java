package eu.pb4.sgui.api;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

public final class GuiHelpers {
    public static void sendSlotUpdate(ServerPlayerEntity player, int syncId, int slot, ItemStack stack, int revision) {
        player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, revision, slot, stack));
    }

    public static void sendSlotUpdate(ServerPlayerEntity player, int syncId, int slot, ItemStack stack) {
        sendSlotUpdate(player, syncId, slot, stack, 0);
    }

    public static void sendPlayerScreenHandler(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new InventoryS2CPacket(player.currentScreenHandler.syncId, player.currentScreenHandler.nextRevision(), player.currentScreenHandler.getStacks(), player.currentScreenHandler.getCursorStack()));
    }

    public static void sendPlayerInventory(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new InventoryS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), player.playerScreenHandler.getStacks(), player.playerScreenHandler.getCursorStack()));
    }

    public static int posToIndex(int x, int y, int height, int width) {
        return x + y * width;
    }

    public static int getHeight(ScreenHandlerType<?> type) {
        if (ScreenHandlerType.GENERIC_9X6.equals(type)) {
            return 6;
        } else if (ScreenHandlerType.GENERIC_9X5.equals(type) || ScreenHandlerType.CRAFTING.equals(type)) {
            return 5;
        } else if (ScreenHandlerType.GENERIC_9X4.equals(type)) {
            return 4;
        } else if (ScreenHandlerType.GENERIC_9X2.equals(type) || ScreenHandlerType.ENCHANTMENT.equals(type) || ScreenHandlerType.STONECUTTER.equals(type)) {
            return 2;
        } else if (ScreenHandlerType.GENERIC_9X1.equals(type) || ScreenHandlerType.BEACON.equals(type) || ScreenHandlerType.HOPPER.equals(type) || ScreenHandlerType.BREWING_STAND.equals(type)) {
            return 1;
        }

        return 3;
    }

    public static int getWidth(ScreenHandlerType<?> type) {
        if (ScreenHandlerType.CRAFTING.equals(type)) {
            return 2;
        } else if (ScreenHandlerType.GENERIC_3X3.equals(type)) {
            return 3;
        } else if (ScreenHandlerType.HOPPER.equals(type) || ScreenHandlerType.BREWING_STAND.equals(type)) {
            return 5;
        } else if (ScreenHandlerType.ENCHANTMENT.equals(type) || ScreenHandlerType.STONECUTTER.equals(type) || ScreenHandlerType.BEACON.equals(type) || ScreenHandlerType.BLAST_FURNACE.equals(type) || ScreenHandlerType.FURNACE.equals(type) || ScreenHandlerType.SMOKER.equals(type) || ScreenHandlerType.ANVIL.equals(type) || ScreenHandlerType.SMITHING.equals(type) || ScreenHandlerType.GRINDSTONE.equals(type) || ScreenHandlerType.MERCHANT.equals(type) || ScreenHandlerType.CARTOGRAPHY_TABLE.equals(type) || ScreenHandlerType.LOOM.equals(type)) {
            return 1;
        }

        return 9;
    }

    private GuiHelpers() {
    }
}
