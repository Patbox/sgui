package eu.pb4.sgui;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class AnvilInputGui extends SimpleGui {
    private String inputText;
    public AnvilInputGui(ServerPlayerEntity player, boolean includePlayer) {
        super(ScreenHandlerType.ANVIL, player, includePlayer);
        this.setDefaultInputValue("");
    }

    public void setDefaultInputValue(String input) {
        ItemStack itemStack = Items.PAPER.getDefaultStack();
        itemStack.setCustomName(new LiteralText(input));
        this.inputText = input;
        this.setSlot(0, itemStack, ((index, type1, action) -> {
            this.reOpen = true;
            this.sendGui();
        }));
    }

    public String getInput() {
        return this.inputText;
    }

    public void input(String input) {
        this.inputText = input;
        this.onInput(input);
    }

    public void onInput(String input) {

    }
}
