package net.alkanphel.kryptonite.power.logic.context;

import net.alkanphel.kryptonite.util.apoli.SavedBlockPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record BlockConditionContext(SavedBlockPosition savedBlockPosition) {

    public BlockConditionContext(Level level, BlockPos pos, BlockState blockState, Optional<BlockEntity> blockEntity) {
        this(level, pos, blockState, blockEntity.orElse(null));
    }

    public BlockConditionContext(Level level, BlockPos pos, BlockState blockState, @Nullable BlockEntity blockEntity) {
        this(new SavedBlockPosition(level, pos, blockState, blockEntity));
    }

    public BlockConditionContext(Level level, BlockPos pos) {
        this(level, pos, level.getBlockState(pos), level.getBlockEntity(pos));
    }

    public Level level() {
        return (Level) savedBlockPosition().getLevel();
    }

    public BlockPos pos() {
        return savedBlockPosition().getPos();
    }

    public BlockState blockState() {
        return savedBlockPosition().getState();
    }

    public Optional<BlockEntity> blockEntity() {
        return Optional.ofNullable(savedBlockPosition().getEntity());
    }

}