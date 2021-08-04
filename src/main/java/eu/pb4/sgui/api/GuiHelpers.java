package eu.pb4.sgui.api;

import net.minecraft.screen.ScreenHandlerType;

public final class GuiHelpers {
    private GuiHelpers() {
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
}
