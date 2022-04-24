package eu.pb4.sgui.testmod;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.*;
import eu.pb4.sgui.api.gui.*;
import eu.pb4.sgui.api.gui.layered.Layer;
import eu.pb4.sgui.api.gui.layered.LayerView;
import eu.pb4.sgui.api.gui.layered.LayeredGui;
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
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

public class SGuiTest implements ModInitializer {

    private static final Random RANDOM = new Random();

    private static int test(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
            SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_3X3, player, false) {
                @Override
                public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
                    this.player.sendMessage(new LiteralText(type.toString()), false);

                    return super.onClick(index, type, action, element);
                }

                @Override
                public void onTick() {
                    this.setSlot(0, new GuiElementBuilder(Items.ARROW).setCount((int) (player.world.getTime() % 127)));
                    super.onTick();
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
            }, 10, false, (x, y, z) -> {
            }));

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
                gui.setSlot(x, new GuiElement(itemStack, (index, clickType, actionType) -> {
                }));
            }

            gui.setSlot(5, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(
                            "ewogICJ0aW1lc3RhbXAiIDogMTYxOTk3MDIyMjQzOCwKICAicHJvZmlsZUlkIiA6ICI2OTBkMDM2OGM2NTE0OGM5ODZjMzEwN2FjMmRjNjFlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5emZyXzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDI0OGVhYTQxNGNjZjA1NmJhOTY5ZTdkODAxZmI2YTkyNzhkMGZlYWUxOGUyMTczNTZjYzhhOTQ2NTY0MzU1ZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                            null, null)
                    .setName(new LiteralText("Battery"))
                    .glow()
            );

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
                    .setCallback((index, clickType, actionType) -> gui.close())
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
            gui.setSlotRedirect(4, new Slot(player.getEnderChestInventory(), 0, 0, 0));

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

            gui.setSlot(30, Items.TNT.getDefaultStack());

            gui.open();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test3(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();

            BookElementBuilder bookBuilder = BookElementBuilder.from(player.getMainHandStack())
                    .addPage(new LiteralText("Test line one!"), new LiteralText("Test line two!"))
                    .addPage(
                            new LiteralText("Click to navigate to page: "),
                            new LiteralText("1").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "1"))),
                            new LiteralText("2").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "2"))),
                            new LiteralText("3").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "3")))
                    )
                    .addPage(new LiteralText("This is page three!"))
                    .setTitle("The Test Book")
                    .setAuthor("aws404");

            BookGui gui = new BookGui(player, bookBuilder) {
                private int tick = 0;

                @Override
                public void onTick() {
                    this.tick++;
                    if (this.tick % 20 == 0) {
                        if (this.page >= WrittenBookItem.getPageCount(getBook()) - 1) {
                            this.setPage(0);
                        } else {
                            this.setPage(getPage() + 1);
                        }
                        this.tick = 0;
                    }
                }

                @Override
                public void onTakeBookButton() {
                    this.getPlayer().giveItemStack(this.getBook().copy());
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
            SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_3X3, player, true) {
                @Override
                public void onClose() {
                    super.onClose();

                    SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, true);
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
                public void onTick() {
                    tick++;
                    if (tick % 30 == 0) {
                        this.setLine(1, new LiteralText(this.getLine(1).asString() + "^"));
                        this.setSignType(Registry.BLOCK.getEntryList(BlockTags.WALL_SIGNS).get().getRandom(RANDOM).get().value());
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

    private static int test7(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
            MerchantGui gui = new MerchantGui(player, false) {

                @Override
                public void onSelectTrade(TradeOffer offer) {
                    this.player.sendMessage(new LiteralText("Selected Trade: " + this.getOfferIndex(offer)), false);
                }

                @Override
                public boolean onTrade(TradeOffer offer) {
                    return player.isCreative();
                }

                @Override
                public void onSuggestSell(TradeOffer offer) {
                    if (offer != null && offer.getSellItem() != null) {
                        offer.getSellItem().setCustomName(((MutableText) player.getName()).append(new LiteralText("'s ")).append(offer.getSellItem().getName()));
                        this.sendUpdate();
                    }
                }
            };

            gui.setTitle(new LiteralText("Trades wow!"));
            gui.setIsLeveled(true);
            gui.addTrade(new TradeOffer(
                    Items.STONE.getDefaultStack(),
                    new GuiElementBuilder(Items.DIAMOND_AXE)
                            .glow()
                            .setCount(1)
                            .setName(new LiteralText("Glowing Axe"))
                            .asStack(),
                    1,
                    0,
                    1
            ));
            gui.open();

            gui.addTrade(new TradeOffer(
                    Items.EMERALD.getDefaultStack(),
                    new GuiElementBuilder(Items.STONE)
                            .setCount(16)
                            .asStack(),
                    100,
                    0,
                    1
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test8(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
            BookInputGui gui = new BookInputGui(player) {
                @Override
                public void onBookWritten(@Nullable String title, List<String> pages, boolean signed) {
                    this.player.sendMessage(new LiteralText("Title was: " + title), false);
                    this.player.sendMessage(new LiteralText("Page 0 was: " + pages.get(0)), false);
                    this.player.sendMessage(new LiteralText("Is signed: " + signed), false);
                    super.onBookWritten(title, pages, signed);
                }
            };

            gui.addPage("Hello world! How's you day?\nNew\nLine!");

            gui.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test9(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
            LayeredGui gui = new LayeredGui(ScreenHandlerType.GENERIC_9X6, player, true);
            GuiElementBuilder elementBuilder = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(LiteralText.EMPTY.copy());
            for (int a = 0; a < 9; a++) {
                for (int b = 0; b < 5; b++) {
                    gui.setSlot(a + (b * 2) * 9, elementBuilder);
                }
            }

            elementBuilder = new GuiElementBuilder(Items.PLAYER_HEAD).setName(LiteralText.EMPTY.copy());
            int i = 1;
            Layer movingLayer = new Layer(2, 3);
            while (movingLayer.getFirstEmptySlot() != -1) {
                elementBuilder.setCount(i++);
                movingLayer.addSlot(elementBuilder);
            }

            LayerView movingView = gui.addLayer(movingLayer, 1, 1);

            Layer controller = new Layer(3, 3);

            controller.setSlot(1, new GuiElementBuilder(Items.SLIME_BALL).setName(new LiteralText("^"))
                    .setCallback((x, y, z) -> movingView.setY(movingView.getY() - 1)));
            controller.setSlot(3, new GuiElementBuilder(Items.SLIME_BALL).setName(new LiteralText("<"))
                    .setCallback((x, y, z) -> movingView.setX(movingView.getX() - 1)));
            controller.setSlot(5, new GuiElementBuilder(Items.SLIME_BALL).setName(new LiteralText(">"))
                    .setCallback((x, y, z) -> movingView.setX(movingView.getX() + 1)));
            controller.setSlot(7, new GuiElementBuilder(Items.SLIME_BALL).setName(new LiteralText("v"))
                    .setCallback((x, y, z) -> movingView.setY(movingView.getY() + 1)));

            controller.setSlot(4, new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).setName(LiteralText.EMPTY.copy()));

            gui.addLayer(controller, 5, 6).setZIndex(5);

            gui.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test10(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
            HotbarGui gui = new HotbarGui(player) {
                int value = 0;

                @Override
                public void onOpen() {
                    player.sendMessage(new LiteralText("OPEN!"), false);
                    super.onOpen();
                }

                @Override
                public void onClose() {
                    player.sendMessage(new LiteralText("CLOSE!"), false);
                }

                @Override
                public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
                    player.sendMessage(new LiteralText("CLICK!"), false);
                    player.sendMessage(new LiteralText(type + " " + index), false);
                    return super.onClick(index, type, action, element);
                }

                @Override
                public void onTick() {
                    this.setSlot(1, new GuiElementBuilder(Items.ARROW).setCount((int) (player.world.getTime() % 127)));
                    super.onTick();
                }

                @Override
                public boolean onSelectedSlotChange(int slot) {
                    if (slot == this.getSelectedSlot()) {
                        return true;
                    }

                    this.value = MathHelper.clamp(this.value + slot - this.getSelectedSlot(), 0, 127);
                    this.setSlot(4, new GuiElementBuilder(Items.POTATO, this.value).setName(new LiteralText("VALUE")));

                    super.onSelectedSlotChange(slot);
                    return true;
                }
            };

            gui.setSelectedSlot(4);

            gui.setSlot(0, new AnimatedGuiElement(new ItemStack[]{
                    Items.NETHERITE_PICKAXE.getDefaultStack(),
                    Items.DIAMOND_PICKAXE.getDefaultStack(),
                    Items.GOLDEN_PICKAXE.getDefaultStack(),
                    Items.IRON_PICKAXE.getDefaultStack(),
                    Items.STONE_PICKAXE.getDefaultStack(),
                    Items.WOODEN_PICKAXE.getDefaultStack()
            }, 10, false, (x, y, z) -> {
            }));

            gui.setSlot(1, new GuiElementBuilder(Items.SPECTRAL_ARROW).setCount((int) (player.world.getTime() % 128)));

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
                gui.setSlot(x, new GuiElement(itemStack, (index, clickType, actionType) -> {
                }));
            }

            gui.setSlot(9, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(
                            "ewogICJ0aW1lc3RhbXAiIDogMTYxOTk3MDIyMjQzOCwKICAicHJvZmlsZUlkIiA6ICI2OTBkMDM2OGM2NTE0OGM5ODZjMzEwN2FjMmRjNjFlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5emZyXzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDI0OGVhYTQxNGNjZjA1NmJhOTY5ZTdkODAxZmI2YTkyNzhkMGZlYWUxOGUyMTczNTZjYzhhOTQ2NTY0MzU1ZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                            null, null)
                    .setName(new LiteralText("Battery"))
                    .glow()
            );

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
                    .setCallback((index, clickType, actionType) -> gui.close())
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
            gui.setSlotRedirect(4, new Slot(player.getEnderChestInventory(), 0, 0, 0));

            gui.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int snake(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
            LayeredGui gui = new SnakeGui(player);
            gui.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


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
            dispatcher.register(
                    literal("test7").executes(SGuiTest::test7)
            );
            dispatcher.register(
                    literal("test8").executes(SGuiTest::test8)
            );
            dispatcher.register(
                    literal("test9").executes(SGuiTest::test9)
            );
            dispatcher.register(
                    literal("test10").executes(SGuiTest::test10)
            );
            dispatcher.register(
                    literal("snake").executes(SGuiTest::snake)
            );
        });
    }
}
