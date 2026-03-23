package eu.pb4.sgui.impl;

import net.minecraft.network.HashedPatchMap;
import net.minecraft.network.HashedStack;
import net.minecraft.world.item.ItemStack;

public record FauxHashedStack() implements HashedStack {
    public static final HashedStack INSTANCE = new FauxHashedStack();
    @Override
    public boolean matches(ItemStack stack, HashedPatchMap.HashGenerator hasher) {
        return false;
    }
}
