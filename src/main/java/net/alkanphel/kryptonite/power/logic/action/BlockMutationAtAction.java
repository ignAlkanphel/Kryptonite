package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.power.logic.mutation.BlockMutation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;

import java.util.List;
import java.util.Optional;

public class BlockMutationAtAction extends Action {

    public static final MapCodec<BlockMutationAtAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockMutation.CODEC.listOf().fieldOf("block_mutations").forGetter(a -> a.blockMutation)
    ).apply(instance, BlockMutationAtAction::new));

    private final List<BlockMutation> blockMutation;

    public BlockMutationAtAction(List<BlockMutation> blockMutation) {
        this.blockMutation = blockMutation;
    }

    @Override
    protected boolean execute(DataContext context) {
        var entity = context.getEntity();
        if (entity == null || !(entity.level() instanceof ServerLevel level)) return false;

        BlockPos pos = BlockPos.containing(entity.position());

        for (BlockMutation mutation : blockMutation) {
            mutation.run(level, pos, Optional.empty());
        }

        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.BLOCK_MUTATION_AT.get();
    }

    public static class Serializer extends ActionSerializer<BlockMutationAtAction> {

        @Override
        public MapCodec<BlockMutationAtAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, BlockMutationAtAction> builder, HolderLookup.Provider provider) {
            builder.setName("Block Mutation At")
                    .setDescription("Performs block mutations at the entity's position.")
                    .add("block_mutations", BlockMutation.documentation(provider), "Block mutation(s) conducted on the blocks.")
                    .addExampleObject(new BlockMutationAtAction(List.of(BlockMutation.ofBoneMeal(false))));
        }
    }

}