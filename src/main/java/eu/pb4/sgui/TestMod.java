package eu.pb4.sgui;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import static net.minecraft.server.command.CommandManager.literal;

public class TestMod {

    public static void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(
                    literal("test").executes(TestMod::test)
            );
            dispatcher.register(
                    literal("test2").executes(TestMod::test2)
            );
        });
    }


    private static int test(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
            SimpleGui gui = new SimpleGui(ScreenHandlerType.BREWING_STAND, player, true) {
                @Override
                public boolean onClick(int index, ClickType type, SlotActionType action, GuiElement element) {
                    this.player.sendMessage(new LiteralText(type.toString()), false);

                    return super.onClick(index, type, action, element);
                }
            };

            gui.setTitle(new LiteralText("Nice"));
            gui.setSlot(0, new GuiElement(Items.TNT.getDefaultStack(), (index, clickType, actionType) -> {
                player.sendMessage(new LiteralText("derg "), false);
                ItemStack item = gui.getSlot(index).getItem();
                if (clickType == ClickType.MOUSE_LEFT) {
                    item.setCount(item.getCount() == 1 ? item.getCount() : item.getCount() - 1);
                } else if (clickType == ClickType.MOUSE_RIGHT) {
                    item.setCount(item.getCount() + 1);
                }
                gui.updateSlot(index, item);
            }));

            for (int x = 1; x < gui.getSize(); x++) {
                ItemStack itemStack = Items.STONE.getDefaultStack();
                itemStack.setCount(x);
                gui.setSlot(x, new GuiElement(itemStack, (index, clickType, actionType) -> {
                }));
            }

            gui.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test2(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
            SimpleGui gui = new SimpleGui(ScreenHandlerType.STONECUTTER, player, true);

            gui.setTitle(new LiteralText("Nice"));
            gui.setSlot(0, new GuiElement(Items.DIAMOND_AXE.getDefaultStack(), (index, clickType, actionType) -> {
                ItemStack item = gui.getSlot(index).getItem();
                if (clickType == ClickType.MOUSE_LEFT) {
                    item.setCount(item.getCount() == 1 ? item.getCount() : item.getCount() - 1);
                } else if (clickType == ClickType.MOUSE_RIGHT) {
                    item.setCount(item.getCount() + 1);
                }
                gui.updateSlot(index, item);
            }));

            gui.open();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
