package eu.pb4.sgui.testmod;

import com.mojang.brigadier.context.CommandContext;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.SguiUtils;
import eu.pb4.sgui.api.ScreenProperty;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static net.minecraft.commands.Commands.literal;

public class SGuiTest implements ModInitializer {

    private static final RandomSource RANDOM = RandomSource.create();

    private static int test(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            ServerPlayer player = objectCommandContext.getSource().getPlayer();
            SimpleGui gui = new SimpleGui(MenuType.GENERIC_3x3, player, false) {
                @Override
                public boolean onClick(int index, ClickType type, ContainerInput action, GuiElement element) {
                    this.player.sendSystemMessage(Component.literal(type.toString()), false);

                    return super.onClick(index, type, action, element);
                }

                @Override
                public void onTick() {
                    this.setSlot(0, new GuiElementBuilder(Items.ARROW).setCount((int) (player.level().getGameTime() % 99999)).setMaxCount(99999));
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
            }, 10, false, (x, y, z, w) -> {
            }));

            gui.setSlot(2, new AnimatedGuiElementBuilder()
                    .setItem(Items.NETHERITE_AXE).setDamage(150).saveItemStack()
                    .setItem(Items.DIAMOND_AXE).setDamage(150).unbreakable().saveItemStack()
                    .setItem(Items.GOLDEN_AXE).glow().saveItemStack()
                    .setItem(Items.IRON_AXE).enchant(objectCommandContext.getSource().registryAccess(), Enchantments.AQUA_AFFINITY, 1).hideDefaultTooltip().saveItemStack()
                    //.setItem(Items.STONE_AXE).noDefaults().saveItemStack()
                    .setItem(Items.WOODEN_AXE).saveItemStack()
                    .setInterval(10).setRandom(true)
            );

            for (int x = 3; x < gui.getSize(); x++) {
                ItemStack itemStack = Items.STONE.getDefaultInstance();
                itemStack.setCount(x);
                gui.setSlot(x, new SimpleGuiElement(itemStack, (index, clickType, actionType, w) -> {
                }));
            }

            gui.setSlot(5, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setProfileSkinTexture(
                            "ewogICJ0aW1lc3RhbXAiIDogMTYxOTk3MDIyMjQzOCwKICAicHJvZmlsZUlkIiA6ICI2OTBkMDM2OGM2NTE0OGM5ODZjMzEwN2FjMmRjNjFlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5emZyXzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDI0OGVhYTQxNGNjZjA1NmJhOTY5ZTdkODAxZmI2YTkyNzhkMGZlYWUxOGUyMTczNTZjYzhhOTQ2NTY0MzU1ZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                            null, null)
                    .setName(Component.literal("Battery"))
                    .glow()
            );

            gui.setSlot(6, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setProfile("patbox")
                    .hideDefaultTooltip()
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
                    .setCallback(() -> gui.close())
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
                    .setCallback((index, clickType, actionType, s) -> {
                        player.sendSystemMessage(Component.literal("derg "), false);
                        ItemStack item = gui.getGuiElement(index).getItemStack();
                        if (clickType == ClickType.MOUSE_LEFT) {
                            item.setCount(item.getCount() == 1 ? item.getCount() : item.getCount() - 1);
                        } else if (clickType == ClickType.MOUSE_RIGHT) {
                            item.setCount(item.getCount() + 1);
                        }
                        ((SimpleGuiElement) gui.getGuiElement(index)).setItemStack(item);

                        if (item.getCount() <= player.getEnderChestInventory().getContainerSize()) {
                            gui.setSlot(4, new Slot(player.getEnderChestInventory(), item.getCount() - 1, 0, 0));
                        }
                    })
            );
            gui.setSlot(4, new Slot(player.getEnderChestInventory(), 0, 0, 0));

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
                public void onManualClose() {
                    player.sendSystemMessage(Component.literal(this.getInput()), false);
                    super.onManualClose();
                }
            };

            gui.setTitle(Component.literal("Nice"));
            gui.setSlot(1, new SimpleGuiElement(Items.DIAMOND_AXE.getDefaultInstance(), (index, clickType, actionType, w) -> {
                ItemStack item = gui.getGuiElement(index).getItemStack();
                if (clickType == ClickType.MOUSE_LEFT) {
                    item.setCount(item.getCount() == 1 ? item.getCount() : item.getCount() - 1);
                } else if (clickType == ClickType.MOUSE_RIGHT) {
                    item.setCount(item.getCount() + 1);
                }
                ((SimpleGuiElement) gui.getGuiElement(index)).setItemStack(item);
            }));

            gui.setSlot(2, new SimpleGuiElement(Items.SLIME_BALL.getDefaultInstance(), (index, clickType, actionType, w) -> {
                player.sendSystemMessage(Component.literal(gui.getInput()), false);
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
                public void onManualClose() {
                    if (this.forceReopen) {
                        this.open();
                    }
                    this.forceReopen = false;
                    super.onManualClose();
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
                public void afterRemoval() {
                    super.afterRemoval();

                    SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x1, player, true);
                    gui.setTitle(Component.literal("If you can take it, it's broken"));
                    gui.setSlot(0, new GuiElementBuilder(Items.DIAMOND, 5));
                    gui.open();
                }
            };

            gui.setSlot(0, new GuiElementBuilder(Items.BARRIER, 8).setCallback(() -> gui.close()));
            gui.setSlot(2, new GuiElementBuilder(Items.IRON_AXE).hideDefaultTooltip());
            gui.setSlot(6, new GuiElementBuilder(Items.BARRIER, 9).setCallback(() -> gui.afterRemoval()));

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
                    this.player.sendSystemMessage(Component.literal(recipeId.toString() + " - " + shift), false);
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
                private int tick = 0;

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
                public void onManualClose() {
                    this.player.sendSystemMessage(Component.literal("Input was: " + this.getLine(0).toString()), false);
                    super.onManualClose();
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
                    this.player.sendSystemMessage(Component.literal("Selected Trade: " + this.getOfferIndex(offer)), false);
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

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test9(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            ServerPlayer player = objectCommandContext.getSource().getPlayer();
            LayeredGui gui = new LayeredGui(MenuType.GENERIC_9x6, player, true);
            GuiElementBuilder elementBuilder = new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.empty());
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
                    .setCallback(() -> movingView.setY(movingView.getY() - 1)));
            controller.setSlot(3, new GuiElementBuilder(Items.SLIME_BALL).setName(Component.literal("<"))
                    .setCallback(() -> movingView.setX(movingView.getX() - 1)));
            controller.setSlot(5, new GuiElementBuilder(Items.SLIME_BALL).setName(Component.literal(">"))
                    .setCallback(() -> movingView.setX(movingView.getX() + 1)));
            controller.setSlot(7, new GuiElementBuilder(Items.SLIME_BALL).setName(Component.literal("v"))
                    .setCallback(() -> movingView.setY(movingView.getY() + 1)));

            controller.setSlot(4, new GuiElementBuilder(Items.STAINED_GLASS_PANE.white()).setName(Component.empty().copy()));

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
                    player.sendSystemMessage(Component.literal("OPEN!"), false);
                    super.onOpen();
                }

                @Override
                public void onManualClose() {
                    player.sendSystemMessage(Component.literal("CLOSE!"), false);
                    super.onManualClose();
                }

                @Override
                public boolean canPlayerClose() {
                    return false;
                }

                @Override
                public boolean onClick(int index, ClickType type, ContainerInput action, GuiElement element) {
                    player.sendSystemMessage(Component.literal("CLICK!"), false);
                    player.sendSystemMessage(Component.literal(type + " " + index), false);
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
            }, 10, false, (x, y, z, w) -> {
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
                gui.setSlot(x, new SimpleGuiElement(itemStack, (_, _, _, _) -> {
                }));
            }

            gui.setSlot(9, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setProfileSkinTexture(
                            "ewogICJ0aW1lc3RhbXAiIDogMTYxOTk3MDIyMjQzOCwKICAicHJvZmlsZUlkIiA6ICI2OTBkMDM2OGM2NTE0OGM5ODZjMzEwN2FjMmRjNjFlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5emZyXzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDI0OGVhYTQxNGNjZjA1NmJhOTY5ZTdkODAxZmI2YTkyNzhkMGZlYWUxOGUyMTczNTZjYzhhOTQ2NTY0MzU1ZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                            null, null)
                    .setName(Component.literal("Battery"))
                    .glow()
            );

            gui.setSlot(6, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setProfile(UUID.fromString("f5a216d9-d660-4996-8d0f-d49053677676"))
                    .setName(Component.literal("Patbox's Head"))
                    .glow()
            );

            gui.setSlot(7, new GuiElementBuilder()
                    .setItem(Items.BARRIER)
                    .glow()
                    .setName(Component.literal("Bye")
                            .setStyle(Style.EMPTY.withItalic(false).withBold(true).applyFormat(ChatFormatting.RED)))
                    .addLoreLine(Component.literal("Some lore"))
                    .addLoreLine(Component.literal("More lore").withStyle(ChatFormatting.RED))
                    .setCount(3)
                    .setCallback(() -> gui.close())
            );

            gui.setSlot(8, new GuiElementBuilder()
                    .setItem(Items.TNT)
                    .glow()
                    .setName(Component.literal("Test :)")
                            .setStyle(Style.EMPTY.withItalic(false).withBold(true)))
                    .addLoreLine(Component.literal("Some lore"))
                    .addLoreLine(Component.literal("More lore").withStyle(ChatFormatting.RED))
                    .setCount(1)
                    .setCallback((index, clickType, _, _) -> {
                        player.sendSystemMessage(Component.literal("derg "), false);
                        ItemStack item = gui.getGuiElement(index).getItemStack();
                        if (clickType == ClickType.MOUSE_LEFT) {
                            item.setCount(item.getCount() == 1 ? item.getCount() : item.getCount() - 1);
                        } else if (clickType == ClickType.MOUSE_RIGHT) {
                            item.setCount(item.getCount() + 1);
                        }
                        ((SimpleGuiElement) gui.getGuiElement(index)).setItemStack(item);

                        if (item.getCount() <= player.getEnderChestInventory().getContainerSize()) {
                            gui.setSlot(4, new Slot(player.getEnderChestInventory(), item.getCount() - 1, 0, 0));
                        }
                    })
            );
            gui.setSlot(4, new Slot(player.getEnderChestInventory(), 0, 0, 0));

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
                var previousGui = SguiUtils.getCurrentGui(player);
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
            var elements = new SimpleGuiElement[1];
            elements[0] = new SimpleGuiElement(new ItemStack(Items.GOLDEN_PICKAXE), (a, type, c, gui) -> {
                if (type != ClickType.OFFHAND_SWAP) {
                    return;
                }
                var offhand = gui.getGuiElement(9);
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

    private static int test15(CommandContext<CommandSourceStack> context) {
        try {
            var player = context.getSource().getPlayerOrException();
            var serverRecipeManager = player.level().recipeAccess();

            var gui = new SimpleGui(MenuType.STONECUTTER, player, false) {
                @Override
                public boolean onButtonClick(int id) {
                    this.setSlot(1, BuiltInRegistries.ITEM.byId(id).getDefaultInstance());
                    this.sendProperty(ScreenProperty.SELECTED, id);
                    return true;
                }

                @Override
                public void onManualClose() {
                    player.connection.send(new ClientboundUpdateRecipesPacket(serverRecipeManager.getSynchronizedItemProperties(), serverRecipeManager.getSynchronizedStonecutterRecipes()));
                    super.onManualClose();
                }
            };

            gui.setSlot(0, new GuiElementBuilder(Items.STONE));

            var list = new ArrayList<SelectableRecipe.SingleInputEntry<StonecutterRecipe>>();

            for (var item : BuiltInRegistries.ITEM) {
                list.add(new SelectableRecipe.SingleInputEntry<>(Ingredient.of(Items.STONE),
                        new SelectableRecipe<>(new SlotDisplay.ItemSlotDisplay(item), Optional.empty())));
            }
            player.connection.send(new ClientboundUpdateRecipesPacket(serverRecipeManager.getSynchronizedItemProperties(), new SelectableRecipe.SingleInputSet<>(list)));
            gui.open();
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
                    literal("test15").executes(SGuiTest::test15)
            );
            dispatcher.register(
                    literal("snake").executes(SGuiTest::snake)
            );
        });
    }
}
