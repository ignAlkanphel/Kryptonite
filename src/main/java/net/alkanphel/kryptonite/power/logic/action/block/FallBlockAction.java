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

public class FallBlockAction extends BlockAction {

    public static final MapCodec<FallBlockAction> CODEC = MapCodec.unit(FallBlockAction::new);

    @Override
    public boolean execute(BlockActionContext context) {
        ServerLevel level = context.level();
        BlockState state = level.getBlockState(context.pos());
        if (state.isAir()) return false;

        FallingBlockEntity.fall(level, context.pos(), state);
        level.setBlockAndUpdate(context.pos(), Blocks.AIR.defaultBlockState());
        return true;
    }

    @Override
    public BlockActionSerializer<?> getSerializer() {
        return BlockActionSerializers.FALL.get();
    }

    public static class Serializer extends BlockActionSerializer<FallBlockAction> {

        @Override
        public MapCodec<FallBlockAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockAction, FallBlockAction> builder, HolderLookup.Provider provider) {
            builder.setName("Fall")
                    .setDescription("Turns the block into a \"minecraft:falling_block\" entity.")
                    .addExampleObject(new FallBlockAction());
        }
    }

}