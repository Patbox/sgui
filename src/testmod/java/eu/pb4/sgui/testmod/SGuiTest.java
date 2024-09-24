package eu.pb4.sgui.testmod;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.*;
import eu.pb4.sgui.api.gui.*;
import eu.pb4.sgui.api.gui.layered.Layer;
import eu.pb4.sgui.api.gui.layered.LayerView;
import eu.pb4.sgui.api.gui.layered.LayeredGui;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static net.minecraft.server.command.CommandManager.literal;

public class SGuiTest implements ModInitializer {

    private static final Random RANDOM = Random.create();

    private static int test(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
            SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_3X3, player, false) {
                @Override
                public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
                    this.player.sendMessage(Text.literal(type.toString()), false);

                    return super.onClick(index, type, action, element);
                }

                @Override
                public void onTick() {
                    this.setSlot(0, new GuiElementBuilder(Items.ARROW).setCount((int) (player.getServerWorld().getTime() % 99999)).setMaxCount(99999));
                    super.onTick();
                }

                @Override
                public boolean canPlayerClose() {
                    return false;
                }
            };

            gui.setTitle(Text.literal("Nice"));
            gui.setSlot(0, new GuiElementBuilder(Items.ARROW).setCount(2000).setMaxDamage(99999));
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
                    .setItem(Items.IRON_AXE).enchant(objectCommandContext.getSource().getRegistryManager(), Enchantments.AQUA_AFFINITY, 1).hideDefaultTooltip().saveItemStack()
                    .setItem(Items.STONE_AXE).noDefaults().saveItemStack()
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
                    .setName(Text.literal("Battery"))
                    .glow()
            );

            gui.setSlot(6, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(new GameProfile(UUID.fromString("f5a216d9-d660-4996-8d0f-d49053677676"), "patbox"), player.server)
                    .setName(Text.literal("Patbox's Head"))
                    .glow()
            );

            gui.setSlot(7, new GuiElementBuilder()
                    .setItem(Items.BARRIER)
                    .glow()
                    .setName(Text.literal("Bye")
                            .setStyle(Style.EMPTY.withItalic(false).withBold(true)))
                    .addLoreLine(Text.literal("Some lore"))
                    .addLoreLine(Text.literal("More lore").formatted(Formatting.RED))
                            .hideTooltip()
                    .setCount(3)
                    .setCallback((index, clickType, actionType) -> gui.close())
            );

            gui.setSlot(8, new GuiElementBuilder()
                    .setItem(Items.TNT)
                    .hideDefaultTooltip()
                    .glow()
                    .setName(Text.literal("Test :)")
                            .setStyle(Style.EMPTY.withItalic(false).withBold(true)))
                    .addLoreLine(Text.literal("Some lore"))
                    .addLoreLine(Text.literal("More lore").formatted(Formatting.RED))
                    .setCount(1)
                    .setCallback((index, clickType, actionType) -> {
                        player.sendMessage(Text.literal("derg "), false);
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
                    player.sendMessage(Text.literal(this.getInput()), false);
                }
            };

            gui.setTitle(Text.literal("Nice"));
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
                player.sendMessage(Text.literal(gui.getInput()), false);
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
                    .addPage(Text.literal("Test line one!"), Text.literal("Test line two!"))
                    .addPage(
                            Text.literal("Click to navigate to page: "),
                            Text.literal("1").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "1"))),
                            Text.literal("2").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "2"))),
                            Text.literal("3").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "3"))),
                            Text.literal("Command").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "Hello World!")))
                    )
                    .addPage(Text.literal("This is page three!"))
                    .setTitle("The Test Book")
                    .setAuthor("aws404");

            BookGui gui = new BookGui(player, bookBuilder) {
                private boolean forceReopen;
                private int tick = 0;

                @Override
                public void onTick() {
                    this.tick++;
                    int pages = getBook().get(DataComponentTypes.WRITTEN_BOOK_CONTENT).pages().size();
                    if (this.tick % 20 == 0) {
                        if (this.page >= pages - 1) {
                            this.setPage(0);
                        } else {
                            this.setPage(getPage() + 1);
                        }
                        this.tick = 0;
                    }
                }

                @Override
                public boolean onCommand(String command) {
                    System.out.println(command);
                    bookBuilder.addPage(Text.of(command));
                    this.book = bookBuilder.asStack();

                    this.forceReopen = true;
                    return true;
                }

                @Override
                public void onClose() {
                    if (this.forceReopen) {
                        this.open();
                    }
                    this.forceReopen = false;
                    super.onClose();
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
                    gui.setTitle(Text.literal("If you can take it, it's broken"));
                    gui.setSlot(0, new GuiElementBuilder(Items.DIAMOND, 5));
                    gui.open();
                }
            };

            gui.setSlot(0, new GuiElementBuilder(Items.BARRIER, 8).setCallback((x, y, z) -> gui.close()));
            gui.setSlot(6, new GuiElementBuilder(Items.BARRIER, 9).setCallback((x, y, z) -> gui.onClose()));

            gui.setTitle(Text.literal("Close gui to test switching"));
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
                    this.player.sendMessage(Text.literal(recipeId.toString() + " - " + shift), false);
                }
            };

            gui.setTitle(Text.literal("Click recipes!"));
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
                    this.setLine(0, ScreenTexts.OK);
                    this.setLine(1, Text.literal("^"));
                    this.setLine(2, Text.literal("Input your"));
                    this.setLine(3, Text.literal("value here"));
                    this.setAutoUpdate(false);
                }

                @Override
                public void onClose() {
                    this.player.sendMessage(Text.literal("Input was: " + this.getLine(0).toString()), false);
                }

                @Override
                public void onTick() {
                    //tick++;
                    //if (tick % 30 == 0) {
                    //    this.setLine(1, Text.literal(this.getLine(1).getString() + "^"));
                     //   this.setSignType(Registries.BLOCK.getEntryList(BlockTags.WALL_SIGNS).get().getRandom(RANDOM).get().value());
                   //     this.setColor(DyeColor.byId(RANDOM.nextInt(15)));
                   //     this.updateSign();
                    //    this.tick = 0;
                  //  }
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
                    this.player.sendMessage(Text.literal("Selected Trade: " + this.getOfferIndex(offer)), false);
                }

                @Override
                public boolean onTrade(TradeOffer offer) {
                    return player.isCreative();
                }

                @Override
                public void onSuggestSell(TradeOffer offer) {
                    if (offer != null && offer.getSellItem() != null) {

                        offer.getSellItem().set(DataComponentTypes.CUSTOM_NAME, ((MutableText) player.getName()).append(Text.literal("'s ")).append(offer.getSellItem().getName()));
                        this.sendUpdate();
                    }
                }
            };

            gui.setTitle(Text.literal("Trades wow!"));
            gui.setIsLeveled(true);
            gui.addTrade(new TradeOffer(
                    new TradedItem(Items.STONE),
                    new GuiElementBuilder(Items.DIAMOND_AXE)
                            .glow()
                            .setCount(1)
                            .setName(Text.literal("Glowing Axe"))
                            .asStack(),
                    1,
                    0,
                    1
            ));
            gui.open();

            gui.addTrade(new TradeOffer(
                    new TradedItem(Items.EMERALD),
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
                    this.player.sendMessage(Text.literal("Title was: " + title), false);
                    this.player.sendMessage(Text.literal("Page 0 was: " + pages.get(0)), false);
                    this.player.sendMessage(Text.literal("Is signed: " + signed), false);
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
            GuiElementBuilder elementBuilder = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.empty());
            for (int a = 0; a < 9; a++) {
                for (int b = 0; b < 5; b++) {
                    gui.setSlot(a + (b * 2) * 9, elementBuilder);
                }
            }

            elementBuilder = new GuiElementBuilder(Items.PLAYER_HEAD).setName(Text.empty());
            int i = 1;
            Layer movingLayer = new Layer(2, 3);
            while (movingLayer.getFirstEmptySlot() != -1) {
                elementBuilder.setCount(i++);
                movingLayer.addSlot(elementBuilder);
            }

            LayerView movingView = gui.addLayer(movingLayer, 1, 1);

            Layer controller = new Layer(3, 3);

            controller.setSlot(1, new GuiElementBuilder(Items.SLIME_BALL).setName(Text.literal("^"))
                    .setCallback((x, y, z) -> movingView.setY(movingView.getY() - 1)));
            controller.setSlot(3, new GuiElementBuilder(Items.SLIME_BALL).setName(Text.literal("<"))
                    .setCallback((x, y, z) -> movingView.setX(movingView.getX() - 1)));
            controller.setSlot(5, new GuiElementBuilder(Items.SLIME_BALL).setName(Text.literal(">"))
                    .setCallback((x, y, z) -> movingView.setX(movingView.getX() + 1)));
            controller.setSlot(7, new GuiElementBuilder(Items.SLIME_BALL).setName(Text.literal("v"))
                    .setCallback((x, y, z) -> movingView.setY(movingView.getY() + 1)));

            controller.setSlot(4, new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).setName(Text.empty().copy()));

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
                    player.sendMessage(Text.literal("OPEN!"), false);
                    super.onOpen();
                }

                @Override
                public void onClose() {
                    player.sendMessage(Text.literal("CLOSE!"), false);
                }

                @Override
                public boolean canPlayerClose() {
                    return false;
                }

                @Override
                public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
                    player.sendMessage(Text.literal("CLICK!"), false);
                    player.sendMessage(Text.literal(type + " " + index), false);
                    return super.onClick(index, type, action, element);
                }

                @Override
                public void onTick() {
                    this.setSlot(1, new GuiElementBuilder(Items.ARROW).setCount((int) (player.getWorld().getTime() % 127)));
                    super.onTick();
                }

                @Override
                public boolean onSelectedSlotChange(int slot) {
                    if (slot == this.getSelectedSlot()) {
                        return true;
                    }

                    this.value = MathHelper.clamp(this.value + slot - this.getSelectedSlot(), 0, 127);
                    this.setSlot(4, new GuiElementBuilder(Items.POTATO, this.value).setName(Text.literal("VALUE")));

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

            gui.setSlot(1, new GuiElementBuilder(Items.SPECTRAL_ARROW).setCount((int) (player.getWorld().getTime() % 128)));

            gui.setSlot(2, new AnimatedGuiElementBuilder()
                    .setItem(Items.NETHERITE_AXE).setDamage(150).saveItemStack()
                    .setItem(Items.DIAMOND_AXE).setDamage(150).unbreakable().saveItemStack()
                    .setItem(Items.GOLDEN_AXE).glow().saveItemStack()
                    .setItem(Items.IRON_AXE).enchant(objectCommandContext.getSource().getRegistryManager(), Enchantments.AQUA_AFFINITY, 1).saveItemStack()
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
                    .setName(Text.literal("Battery"))
                    .glow()
            );

            gui.setSlot(6, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(new GameProfile(UUID.fromString("f5a216d9-d660-4996-8d0f-d49053677676"), "patbox"), player.server)
                    .setName(Text.literal("Patbox's Head"))
                    .glow()
            );

            gui.setSlot(7, new GuiElementBuilder()
                    .setItem(Items.BARRIER)
                    .glow()
                    .setName(Text.literal("Bye")
                            .setStyle(Style.EMPTY.withItalic(false).withBold(true).withFormatting(Formatting.RED)))
                    .addLoreLine(Text.literal("Some lore"))
                    .addLoreLine(Text.literal("More lore").formatted(Formatting.RED))
                    .setCount(3)
                    .setCallback((index, clickType, actionType) -> gui.close())
            );

            gui.setSlot(8, new GuiElementBuilder()
                    .setItem(Items.TNT)
                    .glow()
                    .setName(Text.literal("Test :)")
                            .setStyle(Style.EMPTY.withItalic(false).withBold(true)))
                    .addLoreLine(Text.literal("Some lore"))
                    .addLoreLine(Text.literal("More lore").formatted(Formatting.RED))
                    .setCount(1)
                    .setCallback((index, clickType, actionType) -> {
                        player.sendMessage(Text.literal("derg "), false);
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

    private static int test11(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();

            var num = new MutableInt();
            var creator = new MutableObject<Supplier<SimpleGui>>();
            creator.setValue(() -> {
                var previousGui = GuiHelpers.getCurrentGui(player);
                var gui = new SimpleGui(ScreenHandlerType.HOPPER, player, true);
                var next = new MutableObject<SimpleGui>();
                gui.setTitle(Text.literal("Simple Nested gui test: " + num.getAndIncrement()));
                gui.setSlot(0, new GuiElementBuilder(Items.TRIDENT).setName(Text.literal("Go deeper"))
                        .setCallback(() -> {
                            if (next.getValue() == null) {
                                next.setValue(creator.getValue().get());
                            }

                            next.getValue().open();
                        })
                );

                gui.setSlot(1, new GuiElementBuilder(Items.BARRIER).setName(Text.literal("Go back"))
                        .setCallback(() -> {
                            if (previousGui != null) {
                                previousGui.open();
                            } else {
                                gui.close();
                            }
                        })
                );

                gui.setSlot(10, new GuiElementBuilder(Items.STICK).setCount(num.getValue()));
                return gui;
            });

            creator.getValue().get().open();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test12(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            var player = objectCommandContext.getSource().getPlayerOrThrow();
            player.sendMessage(
                Text.literal("Pickaxe should *only* be able to be swapped only to offhand, both in and out of inventory gui")
            );

            var hotbar = new HotbarGui(player);
            var elements = new GuiElement[1];
            elements[0] = new GuiElement(new ItemStack(Items.GOLDEN_PICKAXE), (a, type, c, gui) -> {
                if (type != ClickType.OFFHAND_SWAP) {
                    return;
                }
                var offhand = gui.getSlot(9);
                if (offhand == null || offhand.getItemStack().isEmpty()) {
                    gui.setSlot(9, elements[0].getItemStack());
                    elements[0].setItemStack(ItemStack.EMPTY);
                } else if (elements[0].getItemStack().isEmpty()) {
                    elements[0].setItemStack(offhand.getItemStack());
                    gui.setSlot(9, ItemStack.EMPTY);
                }
            });
            hotbar.setSlot(0, elements[0]);
            hotbar.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test13(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            var player = objectCommandContext.getSource().getPlayerOrThrow();
            player.getInventory().setStack(PlayerInventory.OFF_HAND_SLOT, new ItemStack(Items.DIAMOND));

            var stack = new ItemStack(Items.GOLDEN_PICKAXE);
            stack.set(
                DataComponentTypes.CUSTOM_NAME,
                Text.literal("Can't swap to offhand")
            );

            var gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, true);
            gui.setTitle(Text.literal("Offhand item should be invisible in gui"));
            gui.setSlot(0, stack);
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
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
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
                    literal("test11").executes(SGuiTest::test11)
            );
            dispatcher.register(
                    literal("test12").executes(SGuiTest::test12)
            );
            dispatcher.register(
                    literal("test13").executes(SGuiTest::test13)
            );
            dispatcher.register(
                    literal("snake").executes(SGuiTest::snake)
            );
        });
    }
}
