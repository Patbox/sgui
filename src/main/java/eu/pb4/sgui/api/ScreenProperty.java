package eu.pb4.sgui.api;

import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.ArrayUtils;

public enum ScreenProperty {
    /**
     * FURNACE
     *
     * The level of the fire icon in the furnace,
     * * Empty = 0
     * * Full = Value of MAX_FUEL_BURN_TIME
     */
    FIRE_LEVEL(0, ScreenHandlerType.FURNACE, ScreenHandlerType.BLAST_FURNACE, ScreenHandlerType.SMOKER),
    /**
     * The maximum burn time of the furnace fuel
     */
    MAX_FUEL_BURN_TIME(1, ScreenHandlerType.FURNACE, ScreenHandlerType.BLAST_FURNACE, ScreenHandlerType.SMOKER),
    /**
     * The current progress ticks of the arrow
     * * No Progress = 0
     * * Complete = Value of MAX_PROGRESS
     */
    CURRENT_PROGRESS(2, ScreenHandlerType.FURNACE, ScreenHandlerType.BLAST_FURNACE, ScreenHandlerType.SMOKER),
    /**
     * The ticks required for the burn to complete (200 on a vanilla server)
     */
    MAX_PROGRESS(3, ScreenHandlerType.FURNACE, ScreenHandlerType.BLAST_FURNACE, ScreenHandlerType.SMOKER),

    /**
     * ENCHANTMENT
     *
     * The level requirement of the respective enchantment
     */
    TOP_LEVEL_REQ(0, ScreenHandlerType.ENCHANTMENT),
    MIDDLE_LEVEL_REQ(1, ScreenHandlerType.ENCHANTMENT),
    BOTTOM_LEVEL_REQ(2, ScreenHandlerType.ENCHANTMENT),
    /**
     * Used for drawing the enchantment names (in SGA) clientside.
     * The same seed is used to calculate enchantments, but some of the data isn't sent to the client to prevent easily guessing the entire list (the seed value here is the regular seed bitwise and 0xFFFFFFF0).
     */
    ENCHANT_SEED(3, ScreenHandlerType.ENCHANTMENT),
    /**
     * The enchantment id of the respective enchantment (set to -1 to hide it)
     * To get the id use {@link Registry#getRawId(Object)} for {@link Registry#ENCHANTMENT}
     */
    TOP_ENCHANTMENT_ID(4, ScreenHandlerType.ENCHANTMENT),
    MIDDLE_ENCHANTMENT_ID(5, ScreenHandlerType.ENCHANTMENT),
    BOTTOM_ENCHANTMENT_ID(6, ScreenHandlerType.ENCHANTMENT),
    /**
     * The enchantment level of the respective enchantment
     * 1 = I
     * 2 = II
     * ...
     * 6 = VI
     * -1 = No Enchantment
     */
    TOP_ENCHANTMENT_LEVEL(7, ScreenHandlerType.ENCHANTMENT),
    MIDDLE_ENCHANTMENT_LEVEL(8, ScreenHandlerType.ENCHANTMENT),
    BOTTOM_ENCHANTMENT_LEVEL(9, ScreenHandlerType.ENCHANTMENT),

    /**
     * BEACON
     *
     * Controls what effect buttons are enabled, equivalent to the number of layers
     * * No Layers = 0
     * ...
     * * Full Beacon = 4
     */
    POWER_LEVEL(0, ScreenHandlerType.BEACON),
    /**
     * The effect id for the respective effect
     * To get the id use {@link Registry#getRawId(Object)} for {@link Registry#POTION}
     */
    FIRST_EFFECT(1, ScreenHandlerType.BEACON),
    SECOND_EFFECT(2, ScreenHandlerType.BEACON),

    /**
     * ANVIL
     *
     * The level cost of the operation. Anything >30 will display as 'Too Expensive!'
     */
    LEVEL_COST(0, ScreenHandlerType.ANVIL),

    /**
     * BREWING_STAND
     *
     * The ticks remaining until the operation completes
     * * Empty Arrow = 400
     * * Full Arrow = 0
     */
    BREW_TIME(0, ScreenHandlerType.BREWING_STAND),
    /**
     * The ticks remaining in the fuel display
     * * Empty Bubbles = 0
     * * Full Bubbles = 20
     */
    FUEL_TIME(1, ScreenHandlerType.BREWING_STAND),

    /**
     * STONECUTTER
     *
     * The index of the selected cut
     * * No Cut Selected = -1
     * * First Cut = 0
     * * Second Cut = 1
     * ...
     */
    SELECTED_CUT(0, ScreenHandlerType.STONECUTTER),

    /**
     * LOOM
     *
     * The index of the selected pattern
     * * No Pattern Selected = -1
     * * First Pattern = 0
     * * Second Pattern = 1
     * ...
     */
    SELECTED_PATTERN(0, ScreenHandlerType.LOOM),

    /**
     * LECTERN
     *
     * The selected page of the book
     * * Page One = 0
     * * Page Two = 1
     * ...
     */
    PAGE_NUMBER(0, ScreenHandlerType.LECTERN);

    private final int id;
    private final ScreenHandlerType<?>[] types;

    ScreenProperty(int id, ScreenHandlerType<?>... types) {
        this.id = id;
        this.types = types;
    }

    public int id() {
        return id;
    }

    public boolean validFor(ScreenHandlerType<?> type) {
        return ArrayUtils.contains(types, type);
    }
}
