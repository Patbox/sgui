package eu.pb4.sgui.testmod;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.layered.Layer;
import eu.pb4.sgui.api.gui.layered.LayeredGui;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnakeGui extends LayeredGui {

    static ItemStack create(BannerPatternLayers component) {
        ItemStack stack = Items.GRAY_BANNER.getDefaultInstance();
        stack.set(DataComponents.BANNER_PATTERNS, component);
        stack.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, ReferenceSortedSets.emptySet()));
        return stack;
    }

    final ItemStack[] NUMBERS;

    final Layer gameplayLayer;
    final Layer scoreLayer;
    Direction direction = Direction.NONE;
    final Layer controller;
    List<Pos> snakeParts;
    List<Pos> apples;
    List<Pos> goldApples = new ArrayList<>();

    Pos snakeHead;
    int ticker = 0;

    int points = 0;
    int applesEaten = 0;

    boolean gameover = false;
    boolean sizeUp = false;

    Random random = new Random();

    public SnakeGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, true);
        this.setTitle(Component.literal("SGui Snake"));
        
        var reg = player.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN);
        
        NUMBERS = new ItemStack[]{
                create(new BannerPatternLayers.Builder().add(reg.get(BannerPatterns.STRIPE_BOTTOM).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.STRIPE_LEFT).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.STRIPE_TOP).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.STRIPE_RIGHT).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.STRIPE_DOWNLEFT).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.BORDER).orElseThrow(), DyeColor.GRAY).build()),
                create(new BannerPatternLayers.Builder().add(reg.get(BannerPatterns.STRIPE_CENTER).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.SQUARE_TOP_LEFT).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.CURLY_BORDER).orElseThrow(), DyeColor.GRAY).add(reg.get(BannerPatterns.STRIPE_BOTTOM).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.BORDER).orElseThrow(), DyeColor.GRAY).build()),
                create(new BannerPatternLayers.Builder().add(reg.get(BannerPatterns.STRIPE_TOP).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.RHOMBUS_MIDDLE).orElseThrow(), DyeColor.GRAY).add(reg.get(BannerPatterns.STRIPE_BOTTOM).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.STRIPE_DOWNLEFT).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.BORDER).orElseThrow(), DyeColor.GRAY).build()),
                create(new BannerPatternLayers.Builder().add(reg.get(BannerPatterns.STRIPE_BOTTOM).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.STRIPE_MIDDLE).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.STRIPE_TOP).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.CURLY_BORDER).orElseThrow(), DyeColor.GRAY).add(reg.get(BannerPatterns.STRIPE_RIGHT).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.BORDER).orElseThrow(), DyeColor.GRAY).build()),
                create(new BannerPatternLayers.Builder().add(reg.get(BannerPatterns.STRIPE_LEFT).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.HALF_HORIZONTAL_MIRROR).orElseThrow(), DyeColor.GRAY).add(reg.get(BannerPatterns.STRIPE_RIGHT).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.STRIPE_MIDDLE).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.BORDER).orElseThrow(), DyeColor.GRAY).build()),
                create(new BannerPatternLayers.Builder().add(reg.get(BannerPatterns.STRIPE_BOTTOM).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.RHOMBUS_MIDDLE).orElseThrow(), DyeColor.GRAY).add(reg.get(BannerPatterns.STRIPE_TOP).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.STRIPE_DOWNRIGHT).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.BORDER).orElseThrow(), DyeColor.GRAY).build()),
                create(new BannerPatternLayers.Builder().add(reg.get(BannerPatterns.STRIPE_BOTTOM).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.STRIPE_RIGHT).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.HALF_HORIZONTAL).orElseThrow(), DyeColor.GRAY).add(reg.get(BannerPatterns.STRIPE_MIDDLE).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.STRIPE_TOP).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.STRIPE_LEFT).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.BORDER).orElseThrow(), DyeColor.GRAY).build()),
                create(new BannerPatternLayers.Builder().add(reg.get(BannerPatterns.STRIPE_DOWNLEFT).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.STRIPE_TOP).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.BORDER).orElseThrow(), DyeColor.GRAY).build()),
                create(new BannerPatternLayers.Builder().add(reg.get(BannerPatterns.STRIPE_DOWNLEFT).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.STRIPE_TOP).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.BORDER).orElseThrow(), DyeColor.GRAY).build()),
                create(new BannerPatternLayers.Builder().add(reg.get(BannerPatterns.STRIPE_LEFT).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.HALF_HORIZONTAL_MIRROR).orElseThrow(), DyeColor.GRAY).add(reg.get(BannerPatterns.STRIPE_MIDDLE).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.STRIPE_TOP).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.STRIPE_RIGHT).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.STRIPE_BOTTOM).orElseThrow(), DyeColor.WHITE).add(reg.get(BannerPatterns.BORDER).orElseThrow(), DyeColor.GRAY).build()),
        };

        Layer controller = new Layer(3, 3);
        this.controller = controller;

        controller.setSlot(1, new GuiElementBuilder(Items.MAGMA_CREAM).setName(Component.literal("^"))
            .setCallback((x, y, z) -> changeDirection(Direction.UP)));

        controller.setSlot(3, new GuiElementBuilder(Items.MAGMA_CREAM).setName(Component.literal("<"))
            .setCallback((x, y, z) -> changeDirection(Direction.LEFT)));

        controller.setSlot(5, new GuiElementBuilder(Items.MAGMA_CREAM).setName(Component.literal(">"))
            .setCallback((x, y, z) -> changeDirection(Direction.RIGHT)));

        controller.setSlot(7, new GuiElementBuilder(Items.MAGMA_CREAM).setName(Component.literal("v"))
            .setCallback((x, y, z) -> changeDirection(Direction.DOWN)));

        controller.setSlot(4, new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).setName(Component.empty()));

        this.addLayer(controller, 3, 6).setZIndex(5);

        this.gameplayLayer = new Layer(6, 9);
        this.addLayer(this.gameplayLayer, 0, 0).setZIndex(1);

        this.snakeParts = new ArrayList<>();
        this.snakeHead = new Pos(4, 2);
        this.snakeParts.add(this.snakeHead);
        this.snakeParts.add(this.snakeHead);
        this.apples = new ArrayList<>();

        Pos applePos = new Pos(this.random.nextInt(9), this.random.nextInt(6));
        if (applePos.equals(this.snakeHead)) {
            applePos = new Pos(3, 6);
        }
        this.apples.add(applePos);

        this.scoreLayer = new Layer(1, 5);
        this.addLayer(this.scoreLayer, 2, 9).setZIndex(1);

        Layer backdrop = new Layer(4, 9);

        GuiElementBuilder builder = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Component.empty());

        while (backdrop.getFirstEmptySlot() != -1) {
            backdrop.addSlot(builder);
        }

        backdrop.setSlot(backdrop.getSize() - 1, new GuiElementBuilder(Items.BARRIER).setName(Component.literal("Close")).setCallback((a, b, c, g) -> g.close()));
        backdrop.setSlot(backdrop.getSize() - 9, new GuiElementBuilder(Items.EMERALD).setName(Component.literal("Restart")).setCallback((a, b, c, g) -> new SnakeGui(this.getPlayer()).open()));
        this.addLayer(backdrop, 0, 6);
    }

    public void changeDirection(Direction direction) {
        if (this.direction == direction || (this.direction.x == -direction.x && this.direction.y == -direction.y)) {
            return;
        }
        if (this.direction.button != -1) {
            GuiElement oldButton = ((GuiElement) this.controller.getSlot(this.direction.button));
            oldButton.setItemStack(GuiElementBuilder.from(oldButton.getItemStack()).setItem(Items.MAGMA_CREAM).asStack());
        }

        this.direction = direction;

        GuiElement button = ((GuiElement) this.controller.getSlot(direction.button));
        button.setItemStack(GuiElementBuilder.from(button.getItemStack()).setItem(Items.SLIME_BALL).asStack());
    }

    @Override
    public void onTick() {
        if (this.direction != Direction.NONE && !gameover) {
            this.ticker++;

            if (this.ticker % 6 == 0) {
                this.snakeParts.add(this.snakeHead);
                if (this.sizeUp) {
                    this.sizeUp = false;
                } else {
                    this.snakeParts.remove(0);
                }
                this.snakeHead = this.snakeHead.of(this.direction);

                if (this.snakeParts.contains(this.snakeHead)) {
                    this.gameover = true;
                    this.setTitle(Component.literal("Game Over!"));
                    return;
                }

                if (this.apples.contains(this.snakeHead)) {
                    this.apples.remove(this.snakeHead);
                    this.sizeUp = true;
                    Pos applePos = new Pos(this.random.nextInt(9), this.random.nextInt(6));

                    while (this.snakeHead.equals(applePos) || this.snakeParts.contains(applePos)) {
                        applePos = new Pos(this.random.nextInt(9), this.random.nextInt(6));
                    }
                    this.apples.add(applePos);
                    this.applesEaten++;

                    if (this.applesEaten % 10 == 0) {
                        Pos gApplePos = new Pos(this.random.nextInt(9), this.random.nextInt(6));

                        while (this.snakeHead.equals(applePos) || this.snakeParts.contains(applePos)) {
                            gApplePos = new Pos(this.random.nextInt(9), this.random.nextInt(6));
                        }

                        this.goldApples.add(gApplePos);
                    }

                    this.points += 10;
                }

                if (this.goldApples.contains(this.snakeHead)) {
                    this.goldApples.remove(this.snakeHead);

                    int size = this.snakeParts.size() / 2;

                    for (int i = 0; i < size; i++) {
                        this.snakeParts.remove(0);
                    }

                    this.points += 100;
                }

            }
        }
        this.gameplayLayer.clearSlots();

        for (Pos pos : this.apples) {
            this.gameplayLayer.setSlot(pos.index(), Items.APPLE.getDefaultInstance());
        }

        for (Pos pos : this.goldApples) {
            this.gameplayLayer.setSlot(pos.index(), Items.GOLDEN_APPLE.getDefaultInstance());
        }

        ItemStack stack = this.gameover ? Items.GRAY_WOOL.getDefaultInstance() : Items.GREEN_WOOL.getDefaultInstance();
        for (Pos pos : this.snakeParts) {
            this.gameplayLayer.setSlot(pos.index(), stack);
        }

        this.gameplayLayer.setSlot(this.snakeHead.index(), this.gameover ? Items.SKELETON_SKULL.getDefaultInstance() : Items.CREEPER_HEAD.getDefaultInstance());

        var scoreComponent = Component.literal("" + this.points);
        for (int x = 0; x < 5; x++) {
            int score = (this.points / (int) Math.pow(10, x)) % 10;
            ItemStack stack1 = NUMBERS[score].copy();
            stack1.set(DataComponents.CUSTOM_NAME, scoreComponent);
            this.scoreLayer.setSlot(4 - x, stack1);
        }

        super.onTick();
    }

    enum Direction {
        NONE(0, 0, -1),
        UP(0, -1, 1),
        DOWN(0, 1, 7),
        LEFT(-1, 0, 3),
        RIGHT(1, 0, 5);

        public final int x;
        public final int y;
        public final int button;

        Direction(int x, int y, int button) {
            this.x = x;
            this.y = y;
            this.button = button;
        }
    }

    record Pos(int x, int y) {
        public int index() {
            return x + y * 9;
        }

        public Pos of(Direction direction) {
            int x = (this.x + direction.x) % 9;
            int y = (this.y + direction.y) % 6;

            return new Pos(x < 0 ? 8 : x, y < 0 ? 5 : y);
        }
    }
}
