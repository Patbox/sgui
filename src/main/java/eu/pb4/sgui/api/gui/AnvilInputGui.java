package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.ApiStatus;

/**
 * Anvil Input Helper
 * <p>
 * The AnvilInputGui is a standard gui for taking Component input from the player.
 * It is superior to a {@link SignGui} as the client sends constant updates
 * of the input back to the server, so filtering and modification can be done
 * on the fly.
 * <p>
 * AnvilInputGui is an implementation of {@link SimpleGui} and thus has all
 * the standard slot and screen modification methods.
 */
@SuppressWarnings({"unused"})
public class AnvilInputGui extends SimpleGui {
    private String inputComponent;
    private String defaultComponent;

    /**
     * Constructs a new input gui for the provided player.
     *
     * @param player                the player to serve this gui to
     * @param manipulatePlayerSlots if <code>true</code> the players inventory
     *                              will be treated as slots of this gui
     */
    public AnvilInputGui(ServerPlayer player, boolean manipulatePlayerSlots) {
        super(MenuType.ANVIL, player, manipulatePlayerSlots);
        this.setDefaultInputValue("");
    }

    /**
     * Sets the default name value for the input (the input stacks name).
     *
     * @param input the default input
     */
    public void setDefaultInputValue(String input) {
        ItemStack itemStack = Items.PAPER.getDefaultInstance();
        itemStack.set(DataComponents.CUSTOM_NAME, Component.literal(input));
        this.inputComponent = input;
        this.defaultComponent = input;
        this.setSlot(0, itemStack, ((index, type1, action, gui) -> {
            this.reOpen = true;
            this.inputComponent = this.defaultComponent;
            this.sendGui();
        }));
    }

    /**
     * Returns the current inputted string
     *
     * @return the current string
     */
    public String getInput() {
        return this.inputComponent;
    }

    /**
     * Executes when the input is changed.
     *
     * @param input the new input
     */
    public void onInput(String input) {
    }

    /**
     * Used internally to receive input from the client
     */
    @ApiStatus.Internal
    public void input(String input) {
        this.inputComponent = input;
        this.onInput(input);
        GuiElementInterface element = this.getSlot(2);
        ItemStack stack = ItemStack.EMPTY;
        if (element != null) {
            stack = element.getItemStack();
        }
        GuiHelpers.sendSlotUpdate(player, this.syncId, 2, stack);
    }
}
