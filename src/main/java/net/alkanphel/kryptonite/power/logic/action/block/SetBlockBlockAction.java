package net.alkanphel.kryptonite.power.logic.action.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockActionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public class SetBlockBlockAction extends BlockAction {

    public static final MapCodec<SetBlockBlockAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.fieldOf("block").forGetter(a -> a.blockState)
    ).apply(instance, SetBlockBlockAction::new));

    private final BlockState blockState;

    public SetBlockBlockAction(BlockState blockState) {
        this.blockState = blockState;
    }

    @Override
    public boolean run(BlockActionContext context) {
        context.level().setBlockAndUpdate(context.pos(), blockState);
        return true;
    }

    @Override
    public BlockActionSerializer<?> getSerializer() {
        return BlockActionSerializers.SET_BLOCK.get();
    }

    public static class Serializer extends BlockActionSerializer<SetBlockBlockAction> {

        @Override
        public MapCodec<SetBlockBlockAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockAction, SetBlockBlockAction> builder, HolderLookup.Provider provider) {
            builder.setName("Set Block")
                    .setDescription("Replaces the block at the current position with the specified block state.")
                    .add("block", TYPE_BLOCK_STATE, "The block state to place.")
                    .addExampleObject(new SetBlockBlockAction(Blocks.STONE.defaultBlockState()));
        }
    }

}