package net.alkanphel.kryptonite.power.logic.context;

import net.minecraft.world.damagesource.DamageSource;

public record DamageFilterContext(DamageSource source, float amount) {}