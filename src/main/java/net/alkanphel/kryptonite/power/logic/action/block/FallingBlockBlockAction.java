package net.alkanphel.kryptonite.power.logic.action.block;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockActionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public class FallingBlockBlockAction extends BlockAction {

    public static final MapCodec<FallingBlockBlockAction> CODEC = MapCodec.unit(FallingBlockBlockAction::new);

    @Override
    public boolean run(BlockActionContext context) {
        ServerLevel level = context.level();
        BlockState state = level.getBlockState(context.pos());
        if (state.isAir()) return false;

        FallingBlockEntity.fall(level, context.pos(), state);
        level.setBlockAndUpdate(context.pos(), Blocks.AIR.defaultBlockState());
        return true;
    }

    @Override
    public BlockActionSerializer<?> getSerializer() {
        return BlockActionSerializers.FALLING_BLOCK.get();
    }

    public static class Serializer extends BlockActionSerializer<FallingBlockBlockAction> {

        @Override
        public MapCodec<FallingBlockBlockAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockAction, FallingBlockBlockAction> builder, HolderLookup.Provider provider) {
            builder.setName("Falling Block")
                    .setDescription("Converts a block into a falling block entity.")
                    .addExampleObject(new FallingBlockBlockAction());
        }
    }

}