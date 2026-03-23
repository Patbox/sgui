package eu.pb4.sgui.api;

import eu.pb4.sgui.api.containerwrappers.AbstractWrapperMenu;
import eu.pb4.sgui.api.gui.GuiLike;
import eu.pb4.sgui.impl.FauxHashedStack;
import eu.pb4.sgui.impl.PlayerExtensions;
import eu.pb4.sgui.mixin.ScreenHandlerAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public final class SguiUtils {
    public static final UnaryOperator<Style> STYLE_CLEARER = style -> style.withItalic(style.isItalic()).withColor(style.getColor() != null ? style.getColor() : TextColor.fromLegacyFormat(ChatFormatting.WHITE));

    @Nullable
    public static GuiLike getCurrentGui(ServerPlayer player) {
        return player.containerMenu instanceof AbstractWrapperMenu v ? v.getBackingGui() : null;
    }

    public static void ignoreNextGuiClosing(ServerPlayer player) {
        ((PlayerExtensions) player).sgui$ignoreNextClose();
    }

    public static void sendSlotUpdate(ServerPlayer player, int syncId, int slot, ItemStack stack, int revision) {
        player.connection.send(new ClientboundContainerSetSlotPacket(syncId, revision, slot, stack));
    }

    public static void sendSlotUpdate(ServerPlayer player, int syncId, int slot, ItemStack stack) {
        sendSlotUpdate(player, syncId, slot, stack, 0);
    }

    public static void sendCurrentMenu(ServerPlayer player) {
        player.connection.send(new ClientboundContainerSetContentPacket(player.containerMenu.containerId, player.containerMenu.incrementStateId(), player.containerMenu.getItems(), player.containerMenu.getCarried()));
    }

    public static void invalidatePlayerInventory(ServerPlayer player) {
        invalidateContainerMenu(player.inventoryMenu);

    }

    public static void invalidateContainerMenu(ServerPlayer player) {
        invalidateContainerMenu(player.containerMenu);
    }

    public static void invalidateContainerMenu(AbstractContainerMenu menu) {
        for (var x : ((ScreenHandlerAccessor) menu).getRemoteSlots()) {
            x.receive(FauxHashedStack.INSTANCE);
        }
    }

    public static void sendPlayerInventory(ServerPlayer player) {
        player.connection.send(new ClientboundContainerSetContentPacket(player.inventoryMenu.containerId, player.inventoryMenu.incrementStateId(), player.inventoryMenu.getItems(), player.inventoryMenu.getCarried()));
    }

    public static int posToIndex(int x, int y, int height, int width) {
        return x + y * width;
    }

    public static int getHeight(MenuType<?> type) {
        if (MenuType.GENERIC_9x6.equals(type)) {
            return 6;
        } else if (MenuType.GENERIC_9x5.equals(type) || MenuType.CRAFTING.equals(type)) {
            return 5;
        } else if (MenuType.GENERIC_9x4.equals(type)) {
            return 4;
        } else if (MenuType.GENERIC_9x2.equals(type) || MenuType.ENCHANTMENT.equals(type) || MenuType.STONECUTTER.equals(type)) {
            return 2;
        } else if (MenuType.GENERIC_9x1.equals(type) || MenuType.BEACON.equals(type) || MenuType.HOPPER.equals(type) || MenuType.BREWING_STAND.equals(type) || MenuType.SMITHING.equals(type)) {
            return 1;
        }

        return 3;
    }

    public static int getWidth(MenuType<?> type) {
        if (MenuType.CRAFTING.equals(type)) {
            return 2;
        } else if (MenuType.SMITHING.equals(type)) {
            return 4;
        } else if (MenuType.GENERIC_3x3.equals(type)) {
            return 3;
        } else if (MenuType.HOPPER.equals(type) || MenuType.BREWING_STAND.equals(type)) {
            return 5;
        } else if (MenuType.ENCHANTMENT.equals(type) || MenuType.STONECUTTER.equals(type) || MenuType.BEACON.equals(type) || MenuType.BLAST_FURNACE.equals(type) || MenuType.FURNACE.equals(type) || MenuType.SMOKER.equals(type) || MenuType.ANVIL.equals(type) || MenuType.GRINDSTONE.equals(type) || MenuType.MERCHANT.equals(type) || MenuType.CARTOGRAPHY_TABLE.equals(type) || MenuType.LOOM.equals(type)) {
            return 1;
        }

        return 9;
    }

    private SguiUtils() {
    }
}
