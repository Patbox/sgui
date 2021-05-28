package eu.pb4.sgui.api.elements;

import com.mojang.authlib.GameProfile;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiElementBuilder implements GuiElementBuilderInterface {
    private Item item = Items.STONE;
    private CompoundTag tag;
    private int count = 1;
    private Text name = null;
    private List<Text> lore = new ArrayList<>();
    private int damage = -1;
    private GuiElement.ItemClickCallback callback = (index, type, action) -> {};
    private byte hideFlags = 0;
    private final Map<Enchantment, Integer> enchantments = new HashMap<>();

    public GuiElementBuilder() {}

    public GuiElementBuilder(Item item, int count) {
        this.item = item;
        this.count = count;
    }

    public GuiElementBuilder(Item item) {
        this.item = item;
    }

    /**
     * Sets the {@link Item} of the {@link ItemStack}
     *
     * @param item the item to use
     */
    public GuiElementBuilder setItem(Item item) {
        this.item = item;
        return this;
    }

    /**
     * Sets the name of the {@link ItemStack}
     *
     * @param name the name to use
     */
    public GuiElementBuilder setName(MutableText name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the number of items in the {@link ItemStack}
     *
     * @param count the number of items
     */
    public GuiElementBuilder setCount(int count) {
        this.count = count;
        return this;
    }

    /**
     * Sets the lore of the {@link ItemStack}
     *
     * @param lore a list of all the lore lines
     */
    public GuiElementBuilder setLore(List<Text> lore) {
        this.lore = lore;
        return this;
    }

    /**
     * Adds a line of lore to the {@link ItemStack}
     *
     * @param lore the line to add
     */
    public GuiElementBuilder addLoreLine(Text lore) {
        this.lore.add(lore);
        return this;
    }

    /**
     * Set the damage of the {@link ItemStack}
     *
     * @param damage the amount of durability the item is missing
     */
    public GuiElementBuilder setDamage(int damage) {
        this.damage = damage;
        return this;
    }

    /**
     * Set the {@link eu.pb4.sgui.api.elements.GuiElementInterface.ItemClickCallback} used inside GUIs
     *
     * @param callback the callback
     */
    public GuiElementBuilder setCallback(GuiElement.ItemClickCallback callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Hide all {@link net.minecraft.item.ItemStack.TooltipSection}s from the {@link ItemStack}s display
     */
    public GuiElementBuilder hideFlags() {
        this.hideFlags = 64;
        return this;
    }

    /**
     * Set the hide flags value for the {@link ItemStack}s display
     *
     * @param value the flags to hide
     */
    public GuiElementBuilder hideFlags(byte value) {
        this.hideFlags = value;
        return this;
    }

    /**
     * Hide a {@link net.minecraft.item.ItemStack.TooltipSection}s from the {@link ItemStack}s display
     *
     * @param section the section to hide
     */
    public GuiElementBuilder hideFlag(ItemStack.TooltipSection section) {
        this.hideFlags = (byte) (this.hideFlags | section.getFlag());
        return this;
    }

    /**
     * Give the {@link ItemStack} the specified enchantment
     *
     * @param enchantment the {@link Enchantment} to apply
     * @param level the level of the specified enchantment
     */
    public GuiElementBuilder enchant(Enchantment enchantment, int level) {
        this.enchantments.put(enchantment, level);
        return this;
    }

    /**
     * Sets the {@link ItemStack} to have an enchantment glint
     */
    public GuiElementBuilder glow() {
        this.enchantments.put(Enchantments.LUCK_OF_THE_SEA, 1);
        return hideFlag(ItemStack.TooltipSection.ENCHANTMENTS);
    }

    /**
     * Sets the custom model data of the {@link ItemStack}
     *
     * @param value the value used for custom model data
     */
    public GuiElementBuilder setCustomModelData(int value) {
        this.getOrCreateTag().putInt("CustomModelData", value);
        return this;
    }

    /**
     * Sets the {@link ItemStack} to be unbreakable, also hiding the durability bar.
     */
    public GuiElementBuilder unbreakable() {
        this.getOrCreateTag().putBoolean("Unbreakable", true);
        return hideFlag(ItemStack.TooltipSection.UNBREAKABLE);
    }

    /**
     * Sets the skull owner tag of a player head.
     * If the server parameter is not supplied it may lag the client while it loads the texture,
     * otherwise if the server is provided and the {@link GameProfile} contains a UUID then the textures will be loaded by the server.
     * This can take some time the first load, however the skins are cached for later uses.
     *
     * @param profile the {@link GameProfile} of the owner
     * @param server the server instance, used to get the textures
     */
    public GuiElementBuilder setSkullOwner(GameProfile profile, @Nullable MinecraftServer server) {
        if (profile.getId() != null && server != null) {
            profile = server.getSessionService().fillProfileProperties(profile, false);
            this.getOrCreateTag().put("SkullOwner", NbtHelper.fromGameProfile(new CompoundTag(), profile));
        } else {
            this.getOrCreateTag().putString("SkullOwner", profile.getName());
        }
        return this;
    }

    private CompoundTag getOrCreateTag() {
        if (this.tag == null) {
            this.tag = new CompoundTag();
        }
        return this.tag;
    }

    public GuiElement build() {
        return new GuiElement(asStack(), this.callback);
    }

    public ItemStack asStack() {
        ItemStack itemStack = new ItemStack(this.item, this.count);

        if (this.tag != null) {
            itemStack.getOrCreateTag().copyFrom(this.tag);
        }

        if (this.name != null) {
            if (this.name instanceof MutableText) {
                ((MutableText) this.name).styled(style -> style.withItalic(style.isItalic()));
            }
            itemStack.setCustomName(this.name);
        }

        if (this.item.isDamageable() && this.damage != -1) {
            itemStack.setDamage(damage);
        }

        for (Map.Entry<Enchantment, Integer> entry : this.enchantments.entrySet()) {
            itemStack.addEnchantment(entry.getKey(), entry.getValue());
        }

        if (this.lore.size() > 0) {
            CompoundTag display = itemStack.getOrCreateSubTag("display");
            ListTag loreItems = new ListTag();
            for (Text l : this.lore) {
                if (l instanceof MutableText) {
                    ((MutableText) l).styled(style -> style.withItalic(style.isItalic()));
                }
                loreItems.add(StringTag.of(Text.Serializer.toJson(l)));
            }
            display.put("Lore", loreItems);
        }

        if (this.hideFlags != 0) {
            itemStack.getOrCreateTag().putByte("HideFlags", this.hideFlags);
        }

        return itemStack;
    }

}
