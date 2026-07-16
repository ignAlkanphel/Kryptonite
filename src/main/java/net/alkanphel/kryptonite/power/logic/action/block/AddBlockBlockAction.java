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

public class AddBlockBlockAction extends BlockAction {

    public static final MapCodec<AddBlockBlockAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.fieldOf("block").forGetter(a -> a.blockState)
    ).apply(instance, AddBlockBlockAction::new));

    private final BlockState blockState;

    public AddBlockBlockAction(BlockState blockState) {
        this.blockState = blockState;
    }

    @Override
    public boolean run(BlockActionContext context) {
        return context.direction().map(dir -> {
            context.level().setBlockAndUpdate(context.pos().relative(dir), blockState);
            return true;
        }).orElse(false);
    }

    @Override
    public BlockActionSerializer<?> getSerializer() {
        return BlockActionSerializers.ADD_BLOCK.get();
    }

    public static class Serializer extends BlockActionSerializer<AddBlockBlockAction> {

        @Override
        public MapCodec<AddBlockBlockAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockAction, AddBlockBlockAction> builder, HolderLookup.Provider provider) {
            builder.setName("Add Block")
                    .setDescription("Adds a block at the specified action position. Adding means setting the block at the position (offset by the direction of the action).")
                    .add("block", TYPE_BLOCK_STATE, "The block state to place.")
                    .addExampleObject(new AddBlockBlockAction(Blocks.STONE.defaultBlockState()));
        }
    }

}