package net.alkanphel.kryptonite.power.logic.context;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.SlotAccess;

public record ItemActionContext(ServerLevel level, SlotAccess slotAccess) {}