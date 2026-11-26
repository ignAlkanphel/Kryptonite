package net.alkanphel.kryptonite.power.logic.context;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.SlotAccess;

public record ItemOperationContext(ServerLevel level, SlotAccess slotAccess) {}