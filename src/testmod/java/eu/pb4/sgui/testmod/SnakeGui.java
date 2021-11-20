package eu.pb4.sgui.testmod;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.layered.Layer;
import eu.pb4.sgui.api.gui.layered.LayeredGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnakeGui extends LayeredGui {
    static ItemStack create(String nbt) {
        ItemStack stack = Items.GRAY_BANNER.getDefaultStack();
        try {
            stack.setNbt(StringNbtReader.parse(nbt));
            stack.addHideFlag(ItemStack.TooltipSection.ADDITIONAL);
        } catch (Exception e) {}

        return stack;
    }

    static ItemStack[] NUMBERS = new ItemStack[] {
            create("{BlockEntityTag:{Patterns:[{Pattern:bs,Color:0},{Pattern:ls,Color:0},{Pattern:ts,Color:0},{Pattern:rs,Color:0},{Pattern:dls,Color:0},{Pattern:bo,Color:7}]}}"),
            create("{BlockEntityTag:{Patterns:[{Pattern:cs,Color:0},{Pattern:tl,Color:0},{Pattern:cbo,Color:7},{Pattern:bs,Color:0},{Pattern:bo,Color:7}]}}"),
            create("{BlockEntityTag:{Patterns:[{Pattern:ts,Color:0},{Pattern:mr,Color:7},{Pattern:bs,Color:0},{Pattern:dls,Color:0},{Pattern:bo,Color:7}]}}"),
            create("{BlockEntityTag:{Patterns:[{Pattern:bs,Color:0},{Pattern:ms,Color:0},{Pattern:ts,Color:0},{Pattern:cbo,Color:7},{Pattern:rs,Color:0},{Pattern:bo,Color:7}]}}"),
            create("{BlockEntityTag:{Patterns:[{Pattern:ls,Color:0},{Pattern:hhb,Color:7},{Pattern:rs,Color:0},{Pattern:ms,Color:0},{Pattern:bo,Color:7}]}}"),
            create("{BlockEntityTag:{Patterns:[{Pattern:bs,Color:0},{Pattern:mr,Color:7},{Pattern:ts,Color:0},{Pattern:drs,Color:0},{Pattern:bo,Color:7}]}}"),
            create("{BlockEntityTag:{Patterns:[{Pattern:bs,Color:0},{Pattern:rs,Color:0},{Pattern:hh,Color:7},{Pattern:ms,Color:0},{Pattern:ts,Color:0},{Pattern:ls,Color:0},{Pattern:bo,Color:7}]}}"),
            create("{BlockEntityTag:{Patterns:[{Pattern:dls,Color:0},{Pattern:ts,Color:0},{Pattern:bo,Color:7}]}}"),
            create("{BlockEntityTag:{Patterns:[{Pattern:dls,Color:0},{Pattern:ts,Color:0},{Pattern:bo,Color:7}]}}"),
            create("{BlockEntityTag:{Patterns:[{Pattern:ls,Color:0},{Pattern:hhb,Color:7},{Pattern:ms,Color:0},{Pattern:ts,Color:0},{Pattern:rs,Color:0},{Pattern:bs,Color:0},{Pattern:bo,Color:7}]}}")
    };


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

    public SnakeGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, true);
        this.setTitle(new LiteralText("SGui Snake"));

        Layer controller = new Layer(3, 3);
        this.controller = controller;

        controller.setSlot(1, new GuiElementBuilder(Items.MAGMA_CREAM).setName(new LiteralText("^"))
                .setCallback((x, y, z) -> changeDirection(Direction.UP)));

        controller.setSlot(3, new GuiElementBuilder(Items.MAGMA_CREAM).setName(new LiteralText("<"))
                .setCallback((x, y, z) -> changeDirection(Direction.LEFT)));

        controller.setSlot(5, new GuiElementBuilder(Items.MAGMA_CREAM).setName(new LiteralText(">"))
                .setCallback((x, y, z) -> changeDirection(Direction.RIGHT)));

        controller.setSlot(7, new GuiElementBuilder(Items.MAGMA_CREAM).setName(new LiteralText("v"))
                .setCallback((x, y, z) -> changeDirection(Direction.DOWN)));

        controller.setSlot(4, new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).setName(LiteralText.EMPTY.copy()));

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
            applePos = new Pos(3,6);
        }
        this.apples.add(applePos);

        this.scoreLayer = new Layer(1, 5);
        this.addLayer(this.scoreLayer, 2, 9).setZIndex(1);

        Layer backdrop = new Layer(4 ,9);

        GuiElementBuilder builder = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(LiteralText.EMPTY.copy());

        while (backdrop.getFirstEmptySlot() != -1) {
            backdrop.addSlot(builder);
        }

        backdrop.setSlot(backdrop.getSize() - 1, new GuiElementBuilder(Items.BARRIER).setName(new LiteralText("Close")).setCallback((a, b, c, g) -> g.close()));
        backdrop.setSlot(backdrop.getSize() - 9, new GuiElementBuilder(Items.EMERALD).setName(new LiteralText("Restart")).setCallback((a, b, c, g) -> new SnakeGui(this.getPlayer()).open()));
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
                    return;
                }

                if (this.apples.contains(this.snakeHead)) {
                    this.apples.remove(this.snakeHead);
                    this.sizeUp = true;
                    Pos applePos = new Pos(this.random.nextInt(9), this.random.nextInt(6));

                    while (this.snakeParts.contains(applePos)) {
                        applePos = new Pos(this.random.nextInt(9), this.random.nextInt(6));
                    }
                    this.apples.add(applePos);
                    this.applesEaten++;

                    if (this.applesEaten % 10 == 0) {
                        Pos gApplePos = new Pos(this.random.nextInt(9), this.random.nextInt(6));

                        while (this.snakeParts.contains(applePos)) {
                            gApplePos = new Pos(this.random.nextInt(9), this.random.nextInt(6));
                        }

                        this.goldApples.add(gApplePos);
                    }

                    this.points += 10;
                }

                if (this.goldApples.contains(this.snakeHead)) {
                    this.goldApples.remove(this.snakeHead);
                    this.snakeParts.remove(0);
                    this.snakeParts.remove(0);
                    this.snakeParts.remove(0);

                    this.points += 100;
                }

            }
        }
        this.gameplayLayer.clearSlots();

        for (Pos pos : this.apples) {
            this.gameplayLayer.setSlot(pos.index(), Items.APPLE.getDefaultStack());
        }

        for (Pos pos : this.goldApples) {
            this.gameplayLayer.setSlot(pos.index(), Items.GOLDEN_APPLE.getDefaultStack());
        }

        ItemStack stack = this.gameover ? Items.GRAY_WOOL.getDefaultStack() : Items.GREEN_WOOL.getDefaultStack();
        for (Pos pos : this.snakeParts) {
            this.gameplayLayer.setSlot(pos.index(), stack);
        }

        this.gameplayLayer.setSlot(this.snakeHead.index(), this.gameover ? Items.SKELETON_SKULL.getDefaultStack() : Items.CREEPER_HEAD.getDefaultStack());

        LiteralText scoreText = new LiteralText("" + this.points);
        for (int x = 0; x < 5; x++) {
            int score = (this.points / (int) Math.pow(10, x)) % 10;
            ItemStack stack1 = NUMBERS[score].copy();
            stack1.setCustomName(scoreText);
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
