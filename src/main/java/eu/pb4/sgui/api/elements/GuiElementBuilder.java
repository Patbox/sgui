package eu.pb4.sgui.api.elements;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.text.Text;

import java.util.*;

public class GuiElementBuilder implements GuiElementBuilderInterface {
    private Item item = Items.STONE;
    private int count = 1;
    private Text name = null;
    private List<Text> lore = new ArrayList<>();
    private GuiElement.ItemClickCallback callback = (index, type, action) -> {};
    private byte hideFlags = 0;
    private Map<Enchantment, Integer> enchantments = new HashMap<>();
    private int customModelData = -1;

    public GuiElementBuilder() {}

    public GuiElementBuilder(Item item, int count) {
        this.item = item;
        this.count = count;
    }

    public GuiElementBuilder(Item item) {
        this.item = item;
    }

    public GuiElementBuilder setItem(Item item) {
        this.item = item;
        return this;
    }

    public GuiElementBuilder setName(Text name) {
        this.name = name;
        return this;
    }

    public GuiElementBuilder setCount(int count) {
        this.count = count;
        return this;
    }

    public GuiElementBuilder setCustomModelData(int value) {
        this.customModelData = value;
        return this;
    }

    public GuiElementBuilder setLore(List<Text> lore) {
        this.lore = lore;
        return this;
    }

    public GuiElementBuilder addLoreLine(Text lore) {
        this.lore.add(lore);
        return this;
    }

    public GuiElementBuilder setCallback(GuiElement.ItemClickCallback callback) {
        this.callback = callback;
        return this;
    }

    public GuiElementBuilder hideFlags() {
        this.hideFlags = 64;
        return this;
    }

    public GuiElementBuilder hideFlags(byte value) {
        this.hideFlags = value;
        return this;
    }

    public GuiElementBuilder enchant(Enchantment enchantment, int level) {
        this.enchantments.put(enchantment, level);
        return this;
    }

    public GuiElementBuilder glow() {
        this.enchantments.put(Enchantments.LUCK_OF_THE_SEA, 1);
        this.hideFlags = (byte) (this.hideFlags | 0x01);
        return this;
    }

    public GuiElement build() {
        ItemStack itemStack = new ItemStack(this.item, this.count);
        if (this.name != null) {
            itemStack.setCustomName(this.name);
        }

        for (Map.Entry<Enchantment, Integer> entry : this.enchantments.entrySet()) {
            itemStack.addEnchantment(entry.getKey(), entry.getValue());
        }

        if (this.lore.size() > 0) {
            CompoundTag display = itemStack.getOrCreateSubTag("display");
            ListTag loreItems = new ListTag();
            for (Text l : this.lore) {
                loreItems.add(StringTag.of(Text.Serializer.toJson(l)));
            }
            display.put("Lore", loreItems);
        }

        itemStack.getOrCreateTag().putByte("HideFlags", this.hideFlags);
        if (this.customModelData != -1) {
            itemStack.getOrCreateTag().putInt("CustomModelData", this.customModelData);
        }

        return new GuiElement(itemStack, this.callback);
    }
}
