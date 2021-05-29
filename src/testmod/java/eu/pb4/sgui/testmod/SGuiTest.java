package eu.pb4.sgui.testmod;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.*;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.BookGui;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Random;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

public class SGuiTest implements ModInitializer {

    private static final Random RANDOM = new Random();

    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(
                literal("test").executes(SGuiTest::test)
            );
            dispatcher.register(
                literal("test2").executes(SGuiTest::test2)
            );
            dispatcher.register(
                    literal("test3").executes(SGuiTest::test3)
            );
            dispatcher.register(
                    literal("test4").executes(SGuiTest::test4)
            );
            dispatcher.register(
                    literal("test5").executes(SGuiTest::test5)
            );
            dispatcher.register(
                    literal("test6").executes(SGuiTest::test6)
            );
        });
    }


    private static int test(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
            SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_3X3, player, false) {
                @Override
                public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
                    this.player.sendMessage(new LiteralText(type.toString()), false);

                    return super.onClick(index, type, action, element);
                }
            };

            gui.setTitle(new LiteralText("Nice"));
            gui.setSlot(0, new GuiElementBuilder(Items.ARROW).setCount(100));
            gui.setSlot(1, new AnimatedGuiElement(new ItemStack[]{
                    Items.NETHERITE_PICKAXE.getDefaultStack(),
                    Items.DIAMOND_PICKAXE.getDefaultStack(),
                    Items.GOLDEN_PICKAXE.getDefaultStack(),
                    Items.IRON_PICKAXE.getDefaultStack(),
                    Items.STONE_PICKAXE.getDefaultStack(),
                    Items.WOODEN_PICKAXE.getDefaultStack()
            }, 10, false, (x, y, z) -> {}));

            gui.setSlot(2, new AnimatedGuiElementBuilder()
                    .setItem(Items.NETHERITE_AXE).setDamage(150).saveItemStack()
                    .setItem(Items.DIAMOND_AXE).setDamage(150).unbreakable().saveItemStack()
                    .setItem(Items.GOLDEN_AXE).glow().saveItemStack()
                    .setItem(Items.IRON_AXE).enchant(Enchantments.AQUA_AFFINITY, 1).hideFlags().saveItemStack()
                    .setItem(Items.STONE_AXE).saveItemStack()
                    .setItem(Items.WOODEN_AXE).saveItemStack()
                    .setInterval(10).setRandom(true)
            );

            for (int x = 3; x < gui.getSize(); x++) {
                ItemStack itemStack = Items.STONE.getDefaultStack();
                itemStack.setCount(x);
                gui.setSlot(x, new GuiElement(itemStack, (index, clickType, actionType) -> {}));
            }

            gui.setSlot(6, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(new GameProfile(UUID.fromString("f5a216d9-d660-4996-8d0f-d49053677676"), "patbox"), player.server)
                    .setName(new LiteralText("Patbox's Head"))
                    .glow()
            );

            gui.setSlot(7, new GuiElementBuilder()
                    .setItem(Items.BARRIER)
                    .glow()
                    .setName(new LiteralText("Bye")
                            .setStyle(Style.EMPTY.withItalic(false).withBold(true)))
                    .addLoreLine(new LiteralText("Some lore"))
                    .addLoreLine(new LiteralText("More lore").formatted(Formatting.RED))
                    .setCount(3)
                    .setCallback((index, clickType, actionType) -> {
                        gui.close();
                    })
            );

            gui.setSlot(8, new GuiElementBuilder()
                    .setItem(Items.TNT)
                    .glow()
                    .setName(new LiteralText("Test :)")
                            .setStyle(Style.EMPTY.withItalic(false).withBold(true)))
                    .addLoreLine(new LiteralText("Some lore"))
                    .addLoreLine(new LiteralText("More lore").formatted(Formatting.RED))
                    .setCount(1)
                    .setCallback((index, clickType, actionType) -> {
                        player.sendMessage(new LiteralText("derg "), false);
                        ItemStack item = gui.getSlot(index).getItemStack();
                        if (clickType == ClickType.MOUSE_LEFT) {
                            item.setCount(item.getCount() == 1 ? item.getCount() : item.getCount() - 1);
                        } else if (clickType == ClickType.MOUSE_RIGHT) {
                            item.setCount(item.getCount() + 1);
                        }
                        ((GuiElement) gui.getSlot(index)).setItemStack(item);

                        if (item.getCount() <= player.getEnderChestInventory().size()) {
                            gui.setSlotRedirect(4, new Slot(player.getEnderChestInventory(), item.getCount() - 1, 0, 0));
                        }
                    })
            );
            gui.setSlotRedirect(4, new Slot(player.getEnderChestInventory(), 0, 0,0));

            gui.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test2(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
            AnvilInputGui gui = new AnvilInputGui(player, true) {
                @Override
                public void onClose() {
                    player.sendMessage(new LiteralText(this.getInput()), false);
                }
            };

            gui.setTitle(new LiteralText("Nice"));
            gui.setSlot(1, new GuiElement(Items.DIAMOND_AXE.getDefaultStack(), (index, clickType, actionType) -> {
                ItemStack item = gui.getSlot(index).getItemStack();
                if (clickType == ClickType.MOUSE_LEFT) {
                    item.setCount(item.getCount() == 1 ? item.getCount() : item.getCount() - 1);
                } else if (clickType == ClickType.MOUSE_RIGHT) {
                    item.setCount(item.getCount() + 1);
                }
                ((GuiElement) gui.getSlot(index)).setItemStack(item);
            }));

            gui.setSlot(2, new GuiElement(Items.SLIME_BALL.getDefaultStack(), (index, clickType, actionType) -> {
                player.sendMessage(new LiteralText(gui.getInput()), false);
            }));

            gui.open();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test3(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
            BookGui gui = new BookGui(player, player.getMainHandStack()) {
                int tick = 0;

                @Override
                public void onTick() {
                    tick++;
                    if (tick % 20 == 0) {
                        if (page >= WrittenBookItem.getPageCount(getBook()) - 1) {
                            setPage(0);
                        } else {
                            setPage(getPage() + 1);
                        }
                        tick = 0;
                    }
                }

                @Override
                public boolean onTakeBookButton() {
                    close();
                    return true;
                }
            };
            gui.open();



        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test4(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
            SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_3X3, player, false) {
                @Override
                public void onClose() {
                    super.onClose();

                    SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);
                    gui.setTitle(new LiteralText("If you can take it, it's broken"));
                    gui.setSlot(0, new GuiElementBuilder(Items.DIAMOND, 5));
                    gui.open();
                }
            };

            gui.setSlot(0, new GuiElementBuilder(Items.BARRIER, 8).setCallback((x, y, z) -> gui.close()));

            gui.setTitle(new LiteralText("Close gui to test switching"));
            gui.open();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test5(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
            SimpleGui gui = new SimpleGui(ScreenHandlerType.CRAFTING, player, false) {
                @Override
                public void onCraftRequest(Identifier recipeId, boolean shift) {
                    super.onCraftRequest(recipeId, shift);
                    this.player.sendMessage(new LiteralText(recipeId.toString() + " - " + shift), false);
                }
            };

            gui.setTitle(new LiteralText("Click recipes!"));
            gui.open();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test6(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
            SignGui gui = new SignGui(player) {
                private int tick = 0;

                {
                    this.setSignType(Blocks.ACACIA_WALL_SIGN);
                    this.setColor(DyeColor.WHITE);
                    this.setLine(1, new LiteralText("^"));
                    this.setLine(2, new LiteralText("Input your"));
                    this.setLine(3, new LiteralText("value here"));
                    this.setAutoUpdate(false);
                }

                @Override
                public void onClose() {
                    this.player.sendMessage(new LiteralText("Input was: " + this.getLine(0).asString()), false);
                }

                @Override
                public void onTick()  {
                    tick++;
                    if (tick % 30 == 0) {
                        this.setLine(1, new LiteralText(this.getLine(1).asString() +  "^"));
                        this.setSignType(BlockTags.WALL_SIGNS.getRandom(RANDOM));
                        this.setColor(DyeColor.byId(RANDOM.nextInt(15)));
                        this.updateSign();
                        this.tick = 0;
                    }
                }
            };
            gui.open();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
