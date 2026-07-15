package net.alkanphel.kryptonite.power.logic.context;

import net.minecraft.world.damagesource.DamageSource;

public record DamageConditionContext(DamageSource source, float amount) {}