package net.alkanphel.kryptonite.power.logic.action.block.internal;

import com.mojang.serialization.Codec;
import net.alkanphel.kryptonite.power.logic.context.BlockActionContext;
import net.alkanphel.kryptonite.registry.KryptoniteRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public abstract class BlockAction {

    public static final Codec<BlockAction> CODEC = KryptoniteRegistries.BLOCK_ACTION_SERIALIZER.byNameCodec().dispatch(BlockAction::getSerializer, BlockActionSerializer::codec);
    public static final Codec<List<BlockAction>> LIST_CODEC = ExtraCodecs.compactListCodec(CODEC);

    public abstract boolean run(BlockActionContext context);

    public void run(Level level, BlockPos pos) {
        this.run(level, pos, Optional.empty());
    }

    public void run(Level level, BlockPos pos, @Nullable Direction direction) {
        this.run(level, pos, Optional.ofNullable(direction));
    }

    public void run(Level level, BlockPos pos, Optional<Direction> direction) {
        if (level instanceof ServerLevel serverLevel) {
            this.run(new BlockActionContext(serverLevel, pos, direction));
        }
    }

    public static boolean runList(List<BlockAction> actions, Level level, BlockPos pos, Optional<Direction> direction) {
        boolean result = false;

        if (level instanceof ServerLevel serverLevel) {
            BlockActionContext context = new BlockActionContext(serverLevel, pos, direction);
            for (BlockAction action : actions) {
                result |= action.run(context);
            }
        }

        return result;
    }

    // original
    public static boolean runList(List<BlockAction> actions, BlockActionContext context) {
        boolean result = false;

        for (BlockAction action : actions) {
            result |= action.run(context);
        }

        return result;
    }

    public abstract BlockActionSerializer<?> getSerializer();

}