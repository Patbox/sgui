package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.virtual.SguiScreenHandlerFactory;
import eu.pb4.sgui.virtual.merchant.VirtualMerchant;
import eu.pb4.sgui.virtual.merchant.VirtualMerchantScreenHandler;
import eu.pb4.sgui.virtual.merchant.VirtualTradeOutputSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.OptionalInt;

/**
 * Merchant Gui Implementation
 * <br>
 * MerchantGui is an implementation of {@link SimpleGui} and thus has all
 * the standard slot and screen modification methods. It also contains
 * various methods and callbacks which can be used modify aspects specific
 * to the merchant screen.
 */
@SuppressWarnings({"unused"})
public class MerchantGui extends SimpleGui {

    protected final VirtualMerchant merchant;
    protected final MerchantContainer merchantInventory;

    /**
     * Constructs a new MerchantGui for the supplied player.
     *
     * @param player                the player to serve this gui to
     * @param manipulatePlayerSlots if <code>true</code> the players inventory
     *                              will be treated as slots of this gui
     */
    public MerchantGui(ServerPlayer player, boolean manipulatePlayerSlots) {
        super(MenuType.MERCHANT, player, manipulatePlayerSlots);
        this.merchant = new VirtualMerchant(player);
        this.merchantInventory = new MerchantContainer(this.merchant);
        this.setTitle(Component.empty());

        this.setSlotRedirect(0, new Slot(this.merchantInventory, 0, 0, 0));
        this.setSlotRedirect(1, new Slot(this.merchantInventory, 1, 0, 0));
        this.setSlotRedirect(2, new VirtualTradeOutputSlot(player, merchant, this.merchantInventory, 2, 0, 0));
    }

    public static boolean areMerchantOffersEqualIgnoreUses(@Nullable MerchantOffer x, @Nullable MerchantOffer y) {
        if (x == null && y == null) {
            return true;
        } else if (x == null || y == null) {
            return false;
        }

        return x.shouldRewardExp() == y.shouldRewardExp()
                && x.getDemand() == y.getDemand()
                && x.getMaxUses() == y.getMaxUses()
                && x.getXp() == y.getXp()
                && x.getSpecialPriceDiff() == y.getSpecialPriceDiff()
                && ItemStack.matches(x.getResult(), y.getResult())
                && ItemStack.matches(x.getBaseCostA(), y.getBaseCostA())
                && ItemStack.matches(x.getItemCostB().map(ItemCost::itemStack).orElse(ItemStack.EMPTY), y.getItemCostB().map(ItemCost::itemStack).orElse(ItemStack.EMPTY));

    }

    /**
     * Adds a new trade to the merchant.
     *
     * @param trade the trade to add
     */
    public void addTrade(MerchantOffer trade) {
        this.merchant.getOffers().add(trade);

        if (this.isOpen() && this.autoUpdate) {
            this.sendUpdate();
        }
    }

    /**
     * Sets the merchant trade at the specified index.
     *
     * @param index the index to replace
     * @param trade the trade to insert
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public void setTrade(int index, MerchantOffer trade) {
        this.merchant.getOffers().add(index, trade);

        if (this.isOpen() && this.autoUpdate) {
            this.sendUpdate();
        }
    }

    /**
     * Sets if merchant leveling is enabled. <br>
     * If disabled, the merchant will not have xp or levels.
     *
     * @param isLeveled is leveling enabled
     */
    public void setIsLeveled(boolean isLeveled) {
        this.merchant.setLeveled(isLeveled);

        if (this.isOpen() && this.autoUpdate) {
            this.sendUpdate();
        }
    }

    /**
     * Get the level of the the merchant.
     *
     * @return the {@link VillagerLevel}
     */
    public VillagerLevel getLevel() {
        return VillagerLevel.values()[this.merchant.getLevel()];
    }

    /**
     * Sets the level of the merchant. <br>
     * Only visible if setIsLeveled has been set to <code>true</code>.
     *
     * @param level the level of the merchant
     */
    public void setLevel(VillagerLevel level) {
        this.merchant.setLevel(level.ordinal());

        if (this.isOpen() && this.autoUpdate) {
            this.sendUpdate();
        }
    }

    /**
     * Gets the experience value of the merchant. <br>
     * Takes into account changes from completed trades.
     *
     * @return the experience of the merchant
     */
    public int getExperience() {
        return this.merchant.getVillagerXp();
    }

    /**
     * Sets the experience value of the merchant. <br>
     * Only visible if setIsLeveled has been set to <code>true</code>. <br>
     * The bar will only display contents when the current <code>experience</code>
     * is larger than the {@link VillagerLevel}s <code>startXp</code>.
     *
     * @param experience the experience of the merchant
     */
    public void setExperience(int experience) {
        this.merchant.overrideXp(experience);

        if (this.isOpen() && this.autoUpdate) {
            this.sendUpdate();
        }
    }

    /**
     * Runs when a trade offer is selected from the list.
     *
     * @param offer the offer selected
     */
    public void onSelectTrade(MerchantOffer offer) {
    }

    /**
     * Gets the last selected trade offer.
     *
     * @return the trade offer or <code>null</code> if none has been selected
     */
    public MerchantOffer getSelectedTrade() {
        return this.merchantInventory.getActiveOffer();
    }

    /**
     * Runs before a trade is completed.
     *
     * @param offer the trade offer being done
     * @return if the trade should complete
     */
    public boolean onTrade(MerchantOffer offer) {
        return true;
    }

    /**
     * Runs when a suggested trade is placed into the selling slot.
     *
     * @param offer the trade that is being suggested.
     */
    public void onSuggestSell(MerchantOffer offer) {
    }

    /**
     * Gets the index of a trade for this merchant.
     *
     * @param offer the trade offer
     * @return the index or <code>-1</code> if the merchant does not have the offer
     */
    public int getOfferIndex(MerchantOffer offer) {
        for (int i = 0; i < this.merchant.getOffers().size(); i++) {
            if (MerchantGui.areMerchantOffersEqualIgnoreUses(this.merchant.getOffers().get(i), offer)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sends an update packet to the player. This will update trades, levels and experience.
     */
    public void sendUpdate() {
        MerchantOffers MerchantOffers = this.merchant.getOffers();
        if (!MerchantOffers.isEmpty()) {
            player.sendMerchantOffers(this.syncId, MerchantOffers, this.merchant.getLevel(), this.merchant.getVillagerXp(), this.merchant.showProgressBar(), this.merchant.canRestock());
        }
    }

    @Override
    protected boolean sendGui() {
        this.reOpen = true;
        OptionalInt opSyncId = player.openMenu(new SguiScreenHandlerFactory<>(this, (syncId, playerInventory, playerx) -> new VirtualMerchantScreenHandler(syncId, this.player, this.merchant, this, this.merchantInventory)));
        if (opSyncId.isPresent()) {
            this.syncId = opSyncId.getAsInt();
            this.screenHandler = (VirtualMerchantScreenHandler) this.player.containerMenu;

            MerchantOffers merchantOfferList = this.merchant.getOffers();
            if (!merchantOfferList.isEmpty()) {
                player.sendMerchantOffers(opSyncId.getAsInt(), merchantOfferList, this.merchant.getLevel(), this.merchant.getVillagerXp(), this.merchant.showProgressBar(), this.merchant.canRestock());
            }

            this.reOpen = false;
            return true;
        }

        this.reOpen = false;
        return false;
    }

    /**
     * Villager Levels
     * <br>
     * These are the 5 different levels that a villager can be. There
     * <code>startXp</code> represents the experience value which will
     * begin to progress on the level bar.
     */
    public enum VillagerLevel {
        /**
         * NONE will still show a level bar however it will never show progress.
         *
         * @see MerchantGui#setIsLeveled(boolean) to disable completelty
         */
        NONE(-1),
        NOVICE(0),
        APPRENTICE(10),
        JOURNEYMAN(70),
        EXPERT(150),
        MASTER(250);

        private static final VillagerLevel[] xpSorted = Arrays.stream(VillagerLevel.values()).sorted((x, y) -> Integer.compare(y.startXp, x.startXp)).toArray(VillagerLevel[]::new);

        public final int startXp;

        VillagerLevel(int startXp) {
            this.startXp = startXp;
        }

        public static VillagerLevel fromId(int id) {
            return VillagerLevel.values()[id];
        }

        public static VillagerLevel fromXp(int xp) {
            for (VillagerLevel value : VillagerLevel.xpSorted) {
                if (xp >= value.startXp) {
                    return value;
                }
            }
            return VillagerLevel.NONE;
        }
    }

}
