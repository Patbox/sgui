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
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static net.minecraft.commands.Commands.literal;


public class SGuiTest implements ModInitializer {

//    private static final RandomSource RANDOM = RandomSource.create();

    private static int test(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            ServerPlayer player = objectCommandContext.getSource().getPlayer();
            SimpleGui gui = new SimpleGui(MenuType.GENERIC_3x3, player, false) {
                @Override
                public boolean onClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action, GuiElementInterface element) {
                    this.player.displayClientMessage(Component.literal(type.toString()), false);

                    return super.onClick(index, type, action, element);
                }

                @Override
                public void onTick() {
                    this.setSlot(0, new GuiElementBuilder(Items.ARROW).setCount((int) (player.serverLevel().getGameTime() % 99999)).setMaxCount(99999));
                    super.onTick();
                }

                @Override
                public boolean canPlayerClose() {
                    return false;
                }
            };

            gui.setTitle(Component.literal("Nice"));
            gui.setSlot(0, new GuiElementBuilder(Items.ARROW).setCount(2000).setMaxDamage(99999));
            gui.setSlot(1, new AnimatedGuiElement(new ItemStack[]{
                    Items.NETHERITE_PICKAXE.getDefaultInstance(),
                    Items.DIAMOND_PICKAXE.getDefaultInstance(),
                    Items.GOLDEN_PICKAXE.getDefaultInstance(),
                    Items.IRON_PICKAXE.getDefaultInstance(),
                    Items.STONE_PICKAXE.getDefaultInstance(),
                    Items.WOODEN_PICKAXE.getDefaultInstance()
            }, 10, false, (x, y, z) -> {
            }));

            gui.setSlot(2, new AnimatedGuiElementBuilder()
                    .setItem(Items.NETHERITE_AXE).setDamage(150).saveItemStack()
                    .setItem(Items.DIAMOND_AXE).setDamage(150).unbreakable().saveItemStack()
                    .setItem(Items.GOLDEN_AXE).glow().saveItemStack()
                    .setItem(Items.IRON_AXE).enchant(objectCommandContext.getSource().registryAccess(), Enchantments.AQUA_AFFINITY, 1).hideDefaultTooltip().saveItemStack()
                    .setItem(Items.STONE_AXE).noDefaults().saveItemStack()
                    .setItem(Items.WOODEN_AXE).saveItemStack()
                    .setInterval(10).setRandom(true)
            );

            for (int x = 3; x < gui.getSize(); x++) {
                ItemStack itemStack = Items.STONE.getDefaultInstance();
                itemStack.setCount(x);
                gui.setSlot(x, new GuiElement(itemStack, (index, clickType, actionType) -> {
                }));
            }

            gui.setSlot(5, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(
                            "ewogICJ0aW1lc3RhbXAiIDogMTYxOTk3MDIyMjQzOCwKICAicHJvZmlsZUlkIiA6ICI2OTBkMDM2OGM2NTE0OGM5ODZjMzEwN2FjMmRjNjFlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5emZyXzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDI0OGVhYTQxNGNjZjA1NmJhOTY5ZTdkODAxZmI2YTkyNzhkMGZlYWUxOGUyMTczNTZjYzhhOTQ2NTY0MzU1ZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                            null, null)
                    .setName(Component.literal("Battery"))
                    .glow()
            );

            gui.setSlot(6, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(new GameProfile(UUID.fromString("f5a216d9-d660-4996-8d0f-d49053677676"), "patbox"), player.server)
                    .setName(Component.literal("# Patbox's Head #"))
                    .glow()
            );

            gui.setSlot(7, new GuiElementBuilder()
                    .setItem(Items.BARRIER)
                    .glow()
                    .setName(Component.literal("Bye")
                            .setStyle(Style.EMPTY.withItalic(false).withBold(true)))
                    .addLoreLine(Component.literal("Some lore"))
                    .addLoreLine(Component.literal("More lore").withStyle(ChatFormatting.RED))
                    .hideTooltip()
                    .setCount(3)
                    .setCallback((index, clickType, actionType) -> gui.close())
            );

            gui.setSlot(8, new GuiElementBuilder()
                    .setItem(Items.TNT)
                    .hideDefaultTooltip()
                    .glow()
                    .setName(Component.literal("Test :)")
                            .setStyle(Style.EMPTY.withItalic(false).withBold(true)))
                    .addLoreLine(Component.literal("Some lore"))
                    .addLoreLine(Component.literal("More lore").withStyle(ChatFormatting.RED))
                    .setCount(1)
                    .setCallback((index, clickType, actionType) -> {
                        player.displayClientMessage(Component.literal("derg "), false);
                        ItemStack item = gui.getSlot(index).getItemStack();
                        if (clickType == ClickType.MOUSE_LEFT) {
                            item.setCount(item.getCount() == 1 ? item.getCount() : item.getCount() - 1);
                        } else if (clickType == ClickType.MOUSE_RIGHT) {
                            item.setCount(item.getCount() + 1);
                        }
                        ((GuiElement) gui.getSlot(index)).setItemStack(item);

                        if (item.getCount() <= player.getEnderChestInventory().getContainerSize()) {
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

    private static int test2(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            ServerPlayer player = objectCommandContext.getSource().getPlayer();
            AnvilInputGui gui = new AnvilInputGui(player, true) {
                @Override
                public void onClose() {
                    player.displayClientMessage(Component.literal(this.getInput()), false);
                }
            };

            gui.setTitle(Component.literal("Nice"));
            gui.setSlot(1, new GuiElement(Items.DIAMOND_AXE.getDefaultInstance(), (index, clickType, actionType) -> {
                ItemStack item = gui.getSlot(index).getItemStack();
                if (clickType == ClickType.MOUSE_LEFT) {
                    item.setCount(item.getCount() == 1 ? item.getCount() : item.getCount() - 1);
                } else if (clickType == ClickType.MOUSE_RIGHT) {
                    item.setCount(item.getCount() + 1);
                }
                ((GuiElement) gui.getSlot(index)).setItemStack(item);
            }));

            gui.setSlot(2, new GuiElement(Items.SLIME_BALL.getDefaultInstance(), (index, clickType, actionType) -> {
                player.displayClientMessage(Component.literal(gui.getInput()), false);
            }));

            gui.setSlot(30, Items.TNT.getDefaultInstance());

            gui.open();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test3(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            ServerPlayer player = objectCommandContext.getSource().getPlayer();

            BookElementBuilder bookBuilder = BookElementBuilder.from(player.getMainHandItem())
                    .addPage(Component.literal("Test line one!"), Component.literal("Test line two!"))
                    .addPage(
                            Component.literal("Click to navigate to page: "),
                            Component.literal("1").withStyle(style -> style.withClickEvent(new ClickEvent.ChangePage(1))),
                            Component.literal("2").withStyle(style -> style.withClickEvent(new ClickEvent.ChangePage(2))),
                            Component.literal("3").withStyle(style -> style.withClickEvent(new ClickEvent.ChangePage(3))),
                            Component.literal("Command").withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand("Hello World!")))
                    )
                    .addPage(Component.literal("This is page three!"))
                    .setTitle("The Test Book")
                    .setAuthor("aws404");

            BookGui gui = new BookGui(player, bookBuilder) {
                private boolean forceReopen;
                private int tick = 0;

                @Override
                public void onTick() {
                    this.tick++;
                    int pages = getBook().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size();
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
                    bookBuilder.addPage(Component.nullToEmpty(command));
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
                    this.getPlayer().addItem(this.getBook().copy());
                }
            };
            gui.open();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test4(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            ServerPlayer player = objectCommandContext.getSource().getPlayer();
            SimpleGui gui = new SimpleGui(MenuType.GENERIC_3x3, player, true) {
                @Override
                public void onClose() {
                    super.onClose();

                    SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x1, player, true);
                    gui.setTitle(Component.literal("If you can take it, it's broken"));
                    gui.setSlot(0, new GuiElementBuilder(Items.DIAMOND, 5));
                    gui.open();
                }
            };

            gui.setSlot(0, new GuiElementBuilder(Items.BARRIER, 8).setCallback((x, y, z) -> gui.close()));
            gui.setSlot(6, new GuiElementBuilder(Items.BARRIER, 9).setCallback((x, y, z) -> gui.onClose()));

            gui.setTitle(Component.literal("Close gui to test switching"));
            gui.open();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test5(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            ServerPlayer player = objectCommandContext.getSource().getPlayer();
            SimpleGui gui = new SimpleGui(MenuType.CRAFTING, player, false) {
                @Override
                public void onCraftRequest(RecipeDisplayId recipeId, boolean shift) {
                    super.onCraftRequest(recipeId, shift);
                    this.player.displayClientMessage(Component.literal(recipeId.toString() + " - " + shift), false);
                }
            };

            gui.setTitle(Component.literal("Click recipes!"));
            gui.open();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test6(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            ServerPlayer player = objectCommandContext.getSource().getPlayer();
            SignGui gui = new SignGui(player) {
                private final int tick = 0;

                {
                    this.setSignType(Blocks.ACACIA_WALL_SIGN);
                    this.setColor(DyeColor.WHITE);
                    this.setLine(0, CommonComponents.GUI_OK);
                    this.setLine(1, Component.literal("^"));
                    this.setLine(2, Component.literal("Input your"));
                    this.setLine(3, Component.literal("value here"));
                    this.setAutoUpdate(false);
                }

                @Override
                public void onClose() {
                    this.player.displayClientMessage(Component.literal("Input was: " + this.getLine(0).toString()), false);
                }

                @Override
                public void onTick() {
                    //tick++;
                    //if (tick % 30 == 0) {
                    //    this.setLine(1, Component.literal(this.getLine(1).getString() + "^"));
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

    private static int test7(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            ServerPlayer player = objectCommandContext.getSource().getPlayer();
            MerchantGui gui = new MerchantGui(player, false) {

                @Override
                public void onSelectTrade(MerchantOffer offer) {
                    this.player.displayClientMessage(Component.literal("Selected Trade: " + this.getOfferIndex(offer)), false);
                }

                @Override
                public boolean onTrade(MerchantOffer offer) {
                    return player.isCreative();
                }

                @Override
                public void onSuggestSell(MerchantOffer offer) {
                    if (offer != null && offer.getResult() != null) {

                        offer.getResult().set(DataComponents.CUSTOM_NAME, ((MutableComponent) player.getName()).append(Component.literal("'s ")).append(offer.getResult().getHoverName()));
                        this.sendUpdate();
                    }
                }
            };

            gui.setTitle(Component.literal("Trades wow!"));
            gui.setIsLeveled(true);
            gui.addTrade(new MerchantOffer(
                    new ItemCost(Items.STONE),
                    new GuiElementBuilder(Items.DIAMOND_AXE)
                            .glow()
                            .setCount(1)
                            .setName(Component.literal("Glowing Axe"))
                            .asStack(),
                    1,
                    0,
                    1
            ));
            gui.open();

            gui.addTrade(new MerchantOffer(
                    new ItemCost(Items.EMERALD),
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

    private static int test8(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            ServerPlayer player = objectCommandContext.getSource().getPlayer();
            BookInputGui gui = new BookInputGui(player) {
                @Override
                public void onBookWritten(@Nullable String title, List<String> pages, boolean signed) {
                    this.player.displayClientMessage(Component.literal("Title was: " + title), false);
                    this.player.displayClientMessage(Component.literal("Page 0 was: " + pages.get(0)), false);
                    this.player.displayClientMessage(Component.literal("Is signed: " + signed), false);
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

    private static int test9(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            ServerPlayer player = objectCommandContext.getSource().getPlayer();
            LayeredGui gui = new LayeredGui(MenuType.GENERIC_9x6, player, true);
            GuiElementBuilder elementBuilder = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Component.empty());
            for (int a = 0; a < 9; a++) {
                for (int b = 0; b < 5; b++) {
                    gui.setSlot(a + (b * 2) * 9, elementBuilder);
                }
            }

            elementBuilder = new GuiElementBuilder(Items.PLAYER_HEAD).setName(Component.empty());
            int i = 1;
            Layer movingLayer = new Layer(2, 3);
            while (movingLayer.getFirstEmptySlot() != -1) {
                elementBuilder.setCount(i++);
                movingLayer.addSlot(elementBuilder);
            }

            LayerView movingView = gui.addLayer(movingLayer, 1, 1);

            Layer controller = new Layer(3, 3);

            controller.setSlot(1, new GuiElementBuilder(Items.SLIME_BALL).setName(Component.literal("^"))
                    .setCallback((x, y, z) -> movingView.setY(movingView.getY() - 1)));
            controller.setSlot(3, new GuiElementBuilder(Items.SLIME_BALL).setName(Component.literal("<"))
                    .setCallback((x, y, z) -> movingView.setX(movingView.getX() - 1)));
            controller.setSlot(5, new GuiElementBuilder(Items.SLIME_BALL).setName(Component.literal(">"))
                    .setCallback((x, y, z) -> movingView.setX(movingView.getX() + 1)));
            controller.setSlot(7, new GuiElementBuilder(Items.SLIME_BALL).setName(Component.literal("v"))
                    .setCallback((x, y, z) -> movingView.setY(movingView.getY() + 1)));

            controller.setSlot(4, new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).setName(Component.empty().copy()));

            gui.addLayer(controller, 5, 6).setZIndex(5);

            gui.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test10(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            ServerPlayer player = objectCommandContext.getSource().getPlayer();
            HotbarGui gui = new HotbarGui(player) {
                int value = 0;

                @Override
                public void onOpen() {
                    player.displayClientMessage(Component.literal("OPEN!"), false);
                    super.onOpen();
                }

                @Override
                public void onClose() {
                    player.displayClientMessage(Component.literal("CLOSE!"), false);
                }

                @Override
                public boolean canPlayerClose() {
                    return false;
                }

                @Override
                public boolean onClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action, GuiElementInterface element) {
                    player.displayClientMessage(Component.literal("CLICK!"), false);
                    player.displayClientMessage(Component.literal(type + " " + index), false);
                    return super.onClick(index, type, action, element);
                }

                @Override
                public void onTick() {
                    this.setSlot(1, new GuiElementBuilder(Items.ARROW).setCount((int) (player.level().getGameTime() % 127)));
                    super.onTick();
                }

                @Override
                public boolean onSelectedSlotChange(int slot) {
                    if (slot == this.getSelectedSlot()) {
                        return true;
                    }

                    this.value = Mth.clamp(this.value + slot - this.getSelectedSlot(), 0, 127);
                    this.setSlot(4, new GuiElementBuilder(Items.POTATO, this.value).setName(Component.literal("VALUE")));

                    super.onSelectedSlotChange(slot);
                    return true;
                }
            };

            gui.setSelectedSlot(4);

            gui.setSlot(0, new AnimatedGuiElement(new ItemStack[]{
                    Items.NETHERITE_PICKAXE.getDefaultInstance(),
                    Items.DIAMOND_PICKAXE.getDefaultInstance(),
                    Items.GOLDEN_PICKAXE.getDefaultInstance(),
                    Items.IRON_PICKAXE.getDefaultInstance(),
                    Items.STONE_PICKAXE.getDefaultInstance(),
                    Items.WOODEN_PICKAXE.getDefaultInstance()
            }, 10, false, (x, y, z) -> {
            }));

            gui.setSlot(1, new GuiElementBuilder(Items.SPECTRAL_ARROW).setCount((int) (player.level().getGameTime() % 128)));

            gui.setSlot(2, new AnimatedGuiElementBuilder()
                    .setItem(Items.NETHERITE_AXE).setDamage(150).saveItemStack()
                    .setItem(Items.DIAMOND_AXE).setDamage(150).unbreakable().saveItemStack()
                    .setItem(Items.GOLDEN_AXE).glow().saveItemStack()
                    .setItem(Items.IRON_AXE).enchant(objectCommandContext.getSource().registryAccess(), Enchantments.AQUA_AFFINITY, 1).saveItemStack()
                    .setItem(Items.STONE_AXE).saveItemStack()
                    .setItem(Items.WOODEN_AXE).saveItemStack()
                    .setInterval(10).setRandom(true)
            );

            for (int x = 3; x < gui.getSize(); x++) {
                ItemStack itemStack = Items.STONE.getDefaultInstance();
                itemStack.setCount(x);
                gui.setSlot(x, new GuiElement(itemStack, (index, clickType, actionType) -> {
                }));
            }

            gui.setSlot(9, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(
                            "ewogICJ0aW1lc3RhbXAiIDogMTYxOTk3MDIyMjQzOCwKICAicHJvZmlsZUlkIiA6ICI2OTBkMDM2OGM2NTE0OGM5ODZjMzEwN2FjMmRjNjFlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5emZyXzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDI0OGVhYTQxNGNjZjA1NmJhOTY5ZTdkODAxZmI2YTkyNzhkMGZlYWUxOGUyMTczNTZjYzhhOTQ2NTY0MzU1ZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                            null, null)
                    .setName(Component.literal("Battery"))
                    .glow()
            );

            gui.setSlot(6, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(new GameProfile(UUID.fromString("f5a216d9-d660-4996-8d0f-d49053677676"), "patbox"), player.server)
                    .setName(Component.literal("Patbox's Head"))
                    .glow()
            );

            gui.setSlot(7, new GuiElementBuilder()
                    .setItem(Items.BARRIER)
                    .glow()
                    .setName(Component.literal("Bye")
                            .setStyle(Style.EMPTY.withItalic(false).withBold(true).applyLegacyFormat(ChatFormatting.RED)))
                    .addLoreLine(Component.literal("Some lore"))
                    .addLoreLine(Component.literal("More lore").withStyle(ChatFormatting.RED))
                    .setCount(3)
                    .setCallback((index, clickType, actionType) -> gui.close())
            );

            gui.setSlot(8, new GuiElementBuilder()
                    .setItem(Items.TNT)
                    .glow()
                    .setName(Component.literal("Test :)")
                            .setStyle(Style.EMPTY.withItalic(false).withBold(true)))
                    .addLoreLine(Component.literal("Some lore"))
                    .addLoreLine(Component.literal("More lore").withStyle(ChatFormatting.RED))
                    .setCount(1)
                    .setCallback((index, clickType, actionType) -> {
                        player.sendSystemMessage(Component.literal("derg "), false);
                        ItemStack item = gui.getSlot(index).getItemStack();
                        if (clickType == ClickType.MOUSE_LEFT) {
                            item.setCount(item.getCount() == 1 ? item.getCount() : item.getCount() - 1);
                        } else if (clickType == ClickType.MOUSE_RIGHT) {
                            item.setCount(item.getCount() + 1);
                        }
                        ((GuiElement) gui.getSlot(index)).setItemStack(item);

                        if (item.getCount() <= player.getEnderChestInventory().getContainerSize()) {
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

    private static int test11(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            ServerPlayer player = objectCommandContext.getSource().getPlayer();

            var num = new MutableInt();
            var creator = new MutableObject<Supplier<SimpleGui>>();
            creator.setValue(() -> {
                var previousGui = GuiHelpers.getCurrentGui(player);
                var gui = new SimpleGui(MenuType.HOPPER, player, true);
                var next = new MutableObject<SimpleGui>();
                gui.setTitle(Component.literal("Simple Nested gui test: " + num.getAndIncrement()));
                gui.setSlot(0, new GuiElementBuilder(Items.TRIDENT).setName(Component.literal("Go deeper"))
                        .setCallback(() -> {
                            if (next.getValue() == null) {
                                next.setValue(creator.getValue().get());
                            }

                            next.getValue().open();
                        })
                );

                gui.setSlot(1, new GuiElementBuilder(Items.BARRIER).setName(Component.literal("Go back"))
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

    private static int test12(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            var player = objectCommandContext.getSource().getPlayerOrException();
            player.sendSystemMessage(
                    Component.literal("Pickaxe should *only* be able to be swapped only to offhand, both in and out of inventory gui")
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

    private static int test13(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            var player = objectCommandContext.getSource().getPlayerOrException();
            player.getInventory().setItem(Inventory.SLOT_OFFHAND, new ItemStack(Items.DIAMOND));

            var stack = new ItemStack(Items.GOLDEN_PICKAXE);
            stack.set(
                    DataComponents.CUSTOM_NAME,
                    Component.literal("Can't swap to offhand")
            );

            var gui = new SimpleGui(MenuType.GENERIC_9x3, player, true);
            gui.setTitle(Component.literal("Offhand item should be invisible in gui"));
            gui.setSlot(0, stack);
            gui.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test14(CommandContext<CommandSourceStack> context) {
        try {
            new TypewriterGui(context.getSource().getPlayerOrException());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int snake(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            ServerPlayer player = objectCommandContext.getSource().getPlayer();
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
                    literal("test14").executes(SGuiTest::test14)
            );
            dispatcher.register(
                    literal("snake").executes(SGuiTest::snake)
            );
        });
    }
}
