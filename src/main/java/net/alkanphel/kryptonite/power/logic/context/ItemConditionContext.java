package net.alkanphel.kryptonite.power.logic.context;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record ItemConditionContext(Level level, ItemStack stack) {}