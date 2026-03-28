package eu.pb4.sgui.api.elements;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import eu.pb4.sgui.api.SguiUtils;
import eu.pb4.sgui.mixin.BuilderAccessor;
import eu.pb4.sgui.mixin.DataComponentPatchAccessor;
import eu.pb4.sgui.mixin.StaticAccessor;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Item Stack Builder
 * <br>
 * The ItemStackBuilder is the best way of constructing item stacks for gui purposes.
 *
 * For method definition, see {@link BaseItemStackBuilder}
 */
@SuppressWarnings({"unused"})
public final class ItemStackBuilder extends BaseItemStackBuilder<ItemStackBuilder> {
    /**
     * Constructs a ItemStackBuilder with the default options
     */
    public ItemStackBuilder() {
    }

    /**
     * Constructs a ItemStackBuilder with the specified Item.
     *
     * @param item the item to use
     */
    public ItemStackBuilder(Item item) {
        this.item = item;
    }

    /**
     * Constructs a ItemStackBuilder with the specified item model.
     *
     * @param model Item model to use. Same as calling model(...).
     */
    public ItemStackBuilder(Identifier model) {
        this.model(model);
    }

    /**
     * Constructs a ItemStackBuilder with the specified Item
     * and number of items.
     *
     * @param item  the item to use
     * @param count the number of items
     */
    public ItemStackBuilder(Item item, int count) {
        this.item = item;
        this.count = count;
    }

    /**
     * Constructs a ItemStackBuilder with the specified ItemStack
     *
     * @param stack  the item stack to use
     */
    public ItemStackBuilder(ItemStack stack) {
        this.item = stack.getItem();
        this.count = stack.getCount();
        ((BuilderAccessor) this.components).getMap().putAll(((DataComponentPatchAccessor) (Object) stack.getComponentsPatch()).getMap());
    }

    public ItemStackBuilder(ItemStackTemplate stack) {
        this.item = stack.item().value();
        this.count = stack.count();
        ((BuilderAccessor) this.components).getMap().putAll(((DataComponentPatchAccessor) (Object) stack.components()).getMap());
    }


    /**
     * Constructs a ItemStackBuilder based on the supplied stack.
     *
     * @param stack the stack to base the builder of
     * @return the constructed builder
     */
    public static ItemStackBuilder from(ItemStack stack) {
        return new ItemStackBuilder(stack);
    }
}
