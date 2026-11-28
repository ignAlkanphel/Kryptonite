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

public class ReplaceBlockAction extends BlockAction {

    public static final MapCodec<ReplaceBlockAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.fieldOf("block").forGetter(a -> a.blockState)
    ).apply(instance, ReplaceBlockAction::new));

    private final BlockState blockState;

    public ReplaceBlockAction(BlockState blockState) {
        this.blockState = blockState;
    }

    @Override
    public boolean execute(BlockActionContext context) {
        context.level().setBlockAndUpdate(context.pos(), blockState);
        return true;
    }

    @Override
    public BlockActionSerializer<?> getSerializer() {
        return BlockActionSerializers.REPLACE.get();
    }

    public static class Serializer extends BlockActionSerializer<ReplaceBlockAction> {

        @Override
        public MapCodec<ReplaceBlockAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockAction, ReplaceBlockAction> builder, HolderLookup.Provider provider) {
            builder.setName("Replace")
                    .setDescription("Replaces a block at the action position.")
                    .add("block", TYPE_BLOCK_STATE, "The block state to place.")
                    .addExampleObject(new ReplaceBlockAction(Blocks.STONE.defaultBlockState()));
        }
    }

}