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

public class PlaceBlockAction extends BlockAction {

    public static final MapCodec<PlaceBlockAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.fieldOf("block").forGetter(a -> a.blockState)
    ).apply(instance, PlaceBlockAction::new));

    private final BlockState blockState;

    public PlaceBlockAction(BlockState blockState) {
        this.blockState = blockState;
    }

    @Override
    public boolean execute(BlockActionContext context) {
        return context.direction().map(dir -> {
            context.level().setBlockAndUpdate(context.pos().relative(dir), blockState);
            return true;
        }).orElse(false);
    }

    @Override
    public BlockActionSerializer<?> getSerializer() {
        return BlockActionSerializers.PLACE.get();
    }

    public static class Serializer extends BlockActionSerializer<PlaceBlockAction> {

        @Override
        public MapCodec<PlaceBlockAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockAction, PlaceBlockAction> builder, HolderLookup.Provider provider) {
            builder.setName("Place")
                    .setDescription("Places a block at the action position.")
                    .add("block", TYPE_BLOCK_STATE, "The block state to place.")
                    .addExampleObject(new PlaceBlockAction(Blocks.STONE.defaultBlockState()));
        }
    }

}