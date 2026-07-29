package net.alkanphel.kryptonite.power.compat.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class KryptoniteCuriosCompat {

    public static KryptoniteCuriosCompat INSTANCE = new KryptoniteCuriosCompat();

    public void preventDropping(LivingEntity entity, List<String> slots, Predicate<ItemStack> predicate) {}

    public List<String> getSlots(Level level) {
        return Collections.emptyList();
    }

    public List<ItemStack> getFromSlot(LivingEntity entity, String slot) {
        return Collections.emptyList();
    }

    public ItemStack getFromSlot(LivingEntity entity, String slot, int index) {
        return ItemStack.EMPTY;
    }

    public void setInSlot(LivingEntity entity, String slot, int index, ItemStack stack) {}

    public int getSlotSize(LivingEntity entity, String slot) {
        return 0;
    }

    public void clearSlot(LivingEntity entity, String slot) {}

}