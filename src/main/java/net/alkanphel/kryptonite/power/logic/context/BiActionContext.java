package net.alkanphel.kryptonite.power.logic.context;

import net.minecraft.world.entity.Entity;

public record BiActionContext(Entity actor, Entity target) {}