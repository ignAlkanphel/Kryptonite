package net.alkanphel.kryptonite.power.logic.context;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

public record BlockActionContext(ServerLevel level, BlockPos pos, Optional<Direction> direction) {}