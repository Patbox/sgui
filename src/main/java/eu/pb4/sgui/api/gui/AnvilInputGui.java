package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.ApiStatus;

/**
 * Anvil Input Helper
 * <p>
 * The AnvilInputGui is a standard gui for taking text input from the player.
 * It is superior to a {@link SignGui} as the client sends constant updates
 * of the input back to the server, so filtering and modification can be done
 * on the fly.
 * <p>
 * AnvilInputGui is an implementation of {@link SimpleGui} and thus has all
 * the standard slot and screen modification methods.
 */
@SuppressWarnings({"unused"})
public class AnvilInputGui extends SimpleGui {
    private String inputText;
    private String defaultText;

    /**
     * Constructs a new input gui for the provided player.
     *
     * @param player        the player to serve this gui to
     * @param includePlayer if <code>true</code> the players inventory
     *                      will be treated as slots of this gui
     */
    public AnvilInputGui(ServerPlayerEntity player, boolean includePlayer) {
        super(ScreenHandlerType.ANVIL, player, includePlayer);
        this.setDefaultInputValue("");
    }

    /**
     * Sets the default name value for the input (the input stacks name).
     *
     * @param input the default input
     */
    public void setDefaultInputValue(String input) {
        ItemStack itemStack = Items.PAPER.getDefaultStack();
        itemStack.setCustomName(new LiteralText(input));
        this.inputText = input;
        this.defaultText = input;
        this.setSlot(0, itemStack, ((index, type1, action, gui) -> {
            this.reOpen = true;
            this.inputText = this.defaultText;
            this.sendGui();
        }));
    }

    /**
     * Returns the current inputted string
     *
     * @return the current string
     */
    public String getInput() {
        return this.inputText;
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
        this.inputText = input;
        this.onInput(input);
        GuiElementInterface element = this.getSlot(2);
        ItemStack stack = ItemStack.EMPTY;
        if (element != null) {
            stack = element.getItemStack();
        }
        GuiHelpers.sendSlotUpdate(player, this.syncId, 2, stack);
    }
}
