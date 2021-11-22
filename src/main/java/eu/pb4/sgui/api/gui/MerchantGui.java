package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.virtual.merchant.VirtualMerchant;
import eu.pb4.sgui.virtual.merchant.VirtualMerchantScreenHandler;
import eu.pb4.sgui.virtual.merchant.VirtualTradeOutputSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.village.MerchantInventory;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
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
    protected final MerchantInventory merchantInventory;

    /**
     * Constructs a new MerchantGui for the supplied player.
     *
     * @param player                      the player to serve this gui to
     * @param includePlayerInventorySlots if <code>true</code> the players inventory
     *                                    will be treated as slots of this gui
     */
    public MerchantGui(ServerPlayerEntity player, boolean includePlayerInventorySlots) {
        super(ScreenHandlerType.MERCHANT, player, includePlayerInventorySlots);
        this.merchant = new VirtualMerchant(player);
        this.merchantInventory = new MerchantInventory(this.merchant);
        this.setTitle(LiteralText.EMPTY);

        this.setSlotRedirect(0, new Slot(this.merchantInventory, 0, 0, 0));
        this.setSlotRedirect(1, new Slot(this.merchantInventory, 1, 0, 0));
        this.setSlotRedirect(2, new VirtualTradeOutputSlot(player, merchant, this.merchantInventory, 2, 0, 0));
    }

    /**
     * Adds a new trade to the merchant.
     *
     * @param trade the trade to add
     */
    public void addTrade(TradeOffer trade) {
        this.merchant.getOffers().add(trade);

        if (this.open && this.autoUpdate) {
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
    public void setTrade(int index, TradeOffer trade) {
        this.merchant.getOffers().add(index, trade);

        if (this.open && this.autoUpdate) {
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

        if (this.open && this.autoUpdate) {
            this.sendUpdate();
        }
    }

    /**
     * Sets the level of the merchant. <br>
     * Only visible if setIsLeveled has been set to <code>true</code>.
     *
     * @param level the level of the merchant
     */
    public void setLevel(VillagerLevel level) {
        this.merchant.setLevel(level.ordinal());

        if (this.open && this.autoUpdate) {
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
     * Sets the experience value of the merchant. <br>
     * Only visible if setIsLeveled has been set to <code>true</code>. <br>
     * The bar will only display contents when the current <code>experience</code>
     * is larger than the {@link VillagerLevel}s <code>startXp</code>.
     *
     * @param experience the experience of the merchant
     */
    public void setExperience(int experience) {
        this.merchant.setExperienceFromServer(experience);

        if (this.open && this.autoUpdate) {
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
        return this.merchant.getExperience();
    }

    /**
     * Runs when a trade offer is selected from the list.
     *
     * @param offer the offer selected
     */
    public void onSelectTrade(TradeOffer offer) {
    }

    /**
     * Gets the last selected trade offer.
     *
     * @return the trade offer or <code>null</code> if none has been selected
     */
    public TradeOffer getSelectedTrade() {
        return this.merchantInventory.getTradeOffer();
    }

    /**
     * Runs before a trade is completed.
     *
     * @param offer the trade offer being done
     * @return if the trade should complete
     */
    public boolean onTrade(TradeOffer offer) {
        return true;
    }

    /**
     * Runs when a suggested trade is placed into the selling slot.
     *
     * @param offer the trade that is being suggested.
     */
    public void onSuggestSell(TradeOffer offer) {
    }

    /**
     * Gets the index of a trade for this merchant.
     *
     * @param offer the trade offer
     * @return the index or <code>-1</code> if the merchant does not have the offer
     */
    public int getOfferIndex(TradeOffer offer) {
        for (int i = 0; i < this.merchant.getOffers().size(); i++) {
            if (MerchantGui.areTradeOffersEqualIgnoreUses(this.merchant.getOffers().get(i), offer)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sends an update packet to the player. This will update trades, levels and experience.
     */
    public void sendUpdate() {
        TradeOfferList tradeOfferList = this.merchant.getOffers();
        if (!tradeOfferList.isEmpty()) {
            player.sendTradeOffers(this.syncId, tradeOfferList, this.merchant.getLevel(), this.merchant.getExperience(), this.merchant.isLeveledMerchant(), this.merchant.canRefreshTrades());
        }
    }

    @Override
    protected boolean sendGui() {
        this.reOpen = true;
        OptionalInt opSyncId = player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, playerx) -> new VirtualMerchantScreenHandler(syncId, this.player, this.merchant, this, this.merchantInventory), this.getTitle()));
        if (opSyncId.isPresent()) {
            this.syncId = opSyncId.getAsInt();
            this.screenHandler = (VirtualMerchantScreenHandler) this.player.currentScreenHandler;

            TradeOfferList tradeOfferList = this.merchant.getOffers();
            if (!tradeOfferList.isEmpty()) {
                player.sendTradeOffers(opSyncId.getAsInt(), tradeOfferList, this.merchant.getLevel(), this.merchant.getExperience(), this.merchant.isLeveledMerchant(), this.merchant.canRefreshTrades());
            }

            this.reOpen = false;
            return true;
        }

        this.reOpen = false;
        return false;
    }

    public static boolean areTradeOffersEqualIgnoreUses(@Nullable TradeOffer x, @Nullable TradeOffer y) {
        if (x == null && y == null) {
            return true;
        } else if (x == null || y == null) {
            return false;
        }

        return x.shouldRewardPlayerExperience() == y.shouldRewardPlayerExperience()
                && x.getDemandBonus() == y.getDemandBonus()
                && x.getMaxUses() == y.getMaxUses()
                && x.getMerchantExperience() == y.getMerchantExperience()
                && x.getSpecialPrice() == y.getSpecialPrice()
                && ItemStack.areEqual(x.getSellItem(), y.getSellItem())
                && ItemStack.areEqual(x.getOriginalFirstBuyItem(), y.getOriginalFirstBuyItem())
                && ItemStack.areEqual(x.getSecondBuyItem(), y.getSecondBuyItem());

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
