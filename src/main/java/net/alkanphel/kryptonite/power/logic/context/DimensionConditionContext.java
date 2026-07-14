package net.alkanphel.kryptonite.power.logic.context;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

public record DimensionConditionContext(DimensionType dimensionType, @Nullable Level level, @Nullable Entity entity) {}