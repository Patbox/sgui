package eu.pb4.sgui.api.elements;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import eu.pb4.sgui.api.SguiUtils;
import eu.pb4.sgui.mixin.BuilderAccessor;
import eu.pb4.sgui.mixin.StaticAccessor;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.Removed;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Item Stack Builder
 * <br>
 * The ItemStackBuilder is the best way of constructing item stacks for gui purposes.
 */
@SuppressWarnings({"unused"})
public class BaseItemStackBuilder<Self extends BaseItemStackBuilder<Self>> {
    protected Item item = Items.TRIAL_KEY;
    protected int count = 1;
    protected DataComponentPatch.Builder components = DataComponentPatch.builder();
    protected boolean hideComponentTooltips;
    protected boolean noTooltips;

    /**
     * Constructs a ItemStackBuilder with the default options
     */
    BaseItemStackBuilder() {
    }

    /**
     * Sets the type of Item of the element.
     *
     * @param item the item to use
     * @return this element builder
     */
    public Self setItem(Item item) {
        this.item = item;
        return (Self) this;
    }

    /**
     * Sets the name of the element.
     *
     * @param name the name to use
     * @return this element builder
     */
    public Self setName(Component name) {
        this.components.set(DataComponents.CUSTOM_NAME, name.copy().withStyle(SguiUtils.STYLE_CLEARER));
        return (Self) this;
    }

    /**
     * Sets the item name of the element.
     *
     * @param name the name to use
     * @return this element builder
     */
    public Self setItemName(Component name) {
        this.components.set(DataComponents.ITEM_NAME, name.copy());
        return (Self) this;
    }

    /**
     * Sets the rarity of the element.
     *
     * @param rarity to use
     * @return this element builder
     */
    public Self setRarity(Rarity rarity) {
        this.components.set(DataComponents.RARITY, rarity);
        return (Self) this;
    }

    /**
     * Sets the number of items in the element.
     *
     * @param count the number of items
     * @return this element builder
     */
    public Self setCount(int count) {
        this.count = count;
        return (Self) this;
    }


    /**
     * Sets the max number of items in the element.
     *
     * @param count the number of items
     * @return this element builder
     */
    public Self setMaxCount(int count) {
        this.components.set(DataComponents.MAX_STACK_SIZE, count);
        return (Self) this;
    }

    /**
     * Sets the lore lines of the element.
     *
     * @param lore a list of all the lore lines
     * @return this element builder
     */
    public Self setLore(List<Component> lore) {
        var l = new ArrayList<Component>(lore.size());
        for (var t : lore) {
            l.add(t.copy().withStyle(SguiUtils.STYLE_CLEARER));
        }

        this.components.set(DataComponents.LORE, new ItemLore(l));
        return (Self) this;
    }

    /**
     * Sets the lore lines of the element, without clearing out formatting.
     *
     * @param lore a list of all the lore lines
     * @return this element builder
     */
    public Self setLoreRaw(List<Component> lore) {
        this.components.set(DataComponents.LORE, new ItemLore(lore));
        return (Self) this;
    }

    /**
     * Adds a line of lore to the element.
     *
     * @param lore the line to add
     * @return this element builder
     */
    public Self addLoreLine(Component lore) {
        //noinspection unchecked
        var component = this.getModifiedComponent(DataComponents.LORE);
        this.components.set(DataComponents.LORE, (component != null ? component : ItemLore.EMPTY).withLineAdded(lore.copy().withStyle(SguiUtils.STYLE_CLEARER)));

        return (Self) this;
    }

    /**
     * Adds a line of lore to the element, without clearing out formatting.
     *
     * @param lore the line to add
     * @return this element builder
     */
    public Self addLoreLineRaw(Component lore) {
        //noinspection unchecked
        var component = this.getModifiedComponent(DataComponents.LORE);
        this.components.set(DataComponents.LORE, (component != null ? component : ItemLore.EMPTY).withLineAdded(lore));
        return (Self) this;
    }

    /**
     * Set the damage of the element. This will only be
     * visible if the item supports has durability.
     *
     * @param damage the amount of durability the item is missing
     * @return this element builder
     */
    public Self setDamage(int damage) {
        this.components.set(DataComponents.DAMAGE, damage);
        return (Self) this;
    }

    /**
     * Set the max damage of the element.
     *
     * @param damage the amount of durability the item is missing
     * @return this element builder
     */
    public Self setMaxDamage(int damage) {
        this.components.set(DataComponents.MAX_DAMAGE, damage);
        return (Self) this;
    }

    @Nullable
    public <T> T getComponent(DataComponentType<T> type) {
        var opt = ((BuilderAccessor) this.components).getMap().get(type);

        //noinspection unchecked,deprecation
        return opt != null ? (T) Removed.removedToNull(opt) : this.item.builtInRegistryHolder().isBound() ? this.item.components().get(type) : null;
    }


    @Nullable
    public <T> T getModifiedComponent(DataComponentType<T> type) {
        var opt = ((BuilderAccessor) this.components).getMap().get(type);

        //noinspection unchecked,deprecation
        return opt != null ? (T) Removed.removedToNull(opt) : null;
    }

    public <T> Self setComponent(DataComponentType<T> type, @Nullable T value) {
        this.components.set(type, value);
        return (Self) this;
    }

    public <T> Self updateComponent(DataComponentType<T> componentType, T defaultValue, Function<T, T> function) {
        var value = this.getComponent(componentType);
        this.setComponent(componentType, function.apply(value != null ? value : defaultValue));
        return (Self) this;
    }

    /**
     * Hides all component-item related tooltip added by item's or non name/lore components.
     *
     * @return this element builder
     */
    public Self hideDefaultTooltip() {
        this.hideComponentTooltips = true;
        return (Self) this;
    }

    /**
     * Hides tooltip completely, making it never show
     * @return this element builder
     */
    public Self hideTooltip() {
        this.noTooltips = true;
        return (Self) this;
    }

    /**
     * Give the element the specified enchantment.
     *
     * @param enchantment the enchantment to apply
     * @param level       the level of the specified enchantment
     * @return this element builder
     */
    public Self enchant(Holder<Enchantment> enchantment, int level) {
        var ench = this.getModifiedComponent(DataComponents.ENCHANTMENTS);
        if (ench == null) {
            ench = ItemEnchantments.EMPTY;
        }

        var mut = new ItemEnchantments.Mutable(ench);
        mut.set(enchantment, level);

        this.components.set(DataComponents.ENCHANTMENTS, mut.toImmutable());
        return (Self) this;
    }

    /**
     * Give the element the specified enchantment.
     *
     * @param server MinecraftServer
     * @param enchantment the enchantment to apply
     * @param level       the level of the specified enchantment
     * @return this element builder
     */
    public Self enchant(MinecraftServer server, ResourceKey<Enchantment> enchantment, int level) {
        return enchant(server.registryAccess(), enchantment, level);
    }

    /**
     * Give the element the specified enchantment.
     *
     * @param lookup WrapperLookup
     * @param enchantment the enchantment to apply
     * @param level       the level of the specified enchantment
     * @return this element builder
     */
    public Self enchant(HolderLookup.Provider lookup, ResourceKey<Enchantment> enchantment, int level) {
        return enchant(lookup.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment), level);
    }

    /**
     * Sets the element to have an enchantment glint.
     *
     * @return this element builder
     */
    public Self glow() {
        this.components.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return (Self) this;
    }

    /**
     * Sets the element to have an enchantment glint.
     *
     * @return this element builder
     */
    public Self glow(boolean value) {
        this.components.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, value);
        return (Self) this;
    }

    /**
     * Sets the custom model data of the element.
     *
     * @return this element builder
     */
    public Self setCustomModelData(List<Float> floats, List<Boolean> flags, List<String> strings, List<Integer> colors) {
        this.components.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(floats, flags, strings, colors));
        return (Self) this;
    }

    /**
     * Sets the model of the element.
     *
     * @param model model to display item as
     * @return this element builder
     */
    public Self model(Identifier model) {
        this.components.set(DataComponents.ITEM_MODEL, model);
        return (Self) this;
    }

    public Self model(Item model) {
        this.components.set(DataComponents.ITEM_MODEL, model.components().get(DataComponents.ITEM_MODEL));
        return (Self) this;
    }

    /**
     * Sets the element to be unbreakable, also hides the durability bar.
     *
     * @return this element builder
     */
    public Self unbreakable() {
        this.components.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
        return (Self) this;
    }

    /**
     * Sets the skull owner tag of a player head.
     * If the server parameter is not supplied it may lag the client while it loads the texture,
     * otherwise if the server is provided and the {@link GameProfile} contains a UUID then the
     * textures will be loaded by the server. This can take some time the first load,
     * however the skins are cached for later uses so its often less noticeable to let the
     * server load the textures.
     *
     * @param profile the {@link GameProfile} of the owner
     * @return this element builder
     */
    public Self setProfile(GameProfile profile) {
        if (!profile.properties().isEmpty()) {
            return this.setProfile(ResolvableProfile.createResolved(profile));
        }
        if (profile.name().isEmpty()) {
            return this.setProfile(ResolvableProfile.createUnresolved(profile.id()));
        }
        if (profile.id().equals(Util.NIL_UUID)) {
            return this.setProfile(ResolvableProfile.createUnresolved(profile.name()));
        }
        return (Self) this;
    }

    public Self setProfile(String name) {
        return this.setProfile(ResolvableProfile.createUnresolved(name));
    }

    public Self setProfile(UUID uuid) {
        return this.setProfile(ResolvableProfile.createUnresolved(uuid));
    }

    public Self setProfile(Identifier textureId) {
        return this.setProfile(StaticAccessor.createStatic(Either.right(ResolvableProfile.Partial.EMPTY),
                new PlayerSkin.Patch(Optional.of(new ClientAsset.ResourceTexture(textureId)), Optional.empty(),
                        Optional.empty(), Optional.empty())));
    }

    public Self setProfile(PlayerSkin.Patch info) {
        return this.setProfile(StaticAccessor.createStatic(Either.right(ResolvableProfile.Partial.EMPTY), info));
    }

    public Self setProfile(ResolvableProfile component) {
        this.components.set(DataComponents.PROFILE, component);
        return (Self) this;
    }


    public Self setProfileSkinTexture(String value) {
        return this.setProfileSkinTexture(value, null, null);
    }

    public Self setProfileSkinTexture(String value, @Nullable String signature, @Nullable UUID uuid) {
        PropertyMap map = new PropertyMap(ImmutableMultimap.of("textures", new Property("textures", value, signature)));
        return this.setProfile(new GameProfile( uuid != null ? uuid : Util.NIL_UUID, "", map));
    }

    /**
     * Constructs an ItemStack using the current builder options.
     * Note that this ignores the callback as it is stored in
     * the {@link SimpleGuiElement}.
     *
     * @return this builder as a stack
     */
    public ItemStack asStack() {
        var copy = new ItemStack(this.item, this.count);
        copy.applyComponents(this.components.build());
        if (this.noTooltips) {
            copy.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, ReferenceSortedSets.emptySet()));
        } else if (this.hideComponentTooltips) {
            var comp = TooltipDisplay.DEFAULT;
            for (var entry : copy.getComponents()) {
                if (entry.type() != DataComponents.ITEM_NAME && entry.type() != DataComponents.CUSTOM_NAME && entry.type() != DataComponents.LORE) {
                    comp = comp.withHidden(entry.type(), true);
                }
            }
            copy.set(DataComponents.TOOLTIP_DISPLAY, comp);
        }

        return copy;
    }
}
