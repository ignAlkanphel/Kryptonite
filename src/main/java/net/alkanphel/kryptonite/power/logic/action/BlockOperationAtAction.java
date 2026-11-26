package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.power.logic.operation.BlockOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;

import java.util.List;
import java.util.Optional;

public class BlockOperationAtAction extends Action {

    public static final MapCodec<BlockOperationAtAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockOperation.CODEC.listOf().fieldOf("block_operations").forGetter(a -> a.blockOperation)
    ).apply(instance, BlockOperationAtAction::new));

    private final List<BlockOperation> blockOperation;

    public BlockOperationAtAction(List<BlockOperation> blockOperation) {
        this.blockOperation = blockOperation;
    }

    @Override
    protected boolean execute(DataContext context) {
        var entity = context.getEntity();
        if (entity == null || !(entity.level() instanceof ServerLevel level)) return false;

        BlockPos pos = BlockPos.containing(entity.position());

        for (BlockOperation operation : blockOperation) {
            operation.run(level, pos, Optional.empty());
        }

        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.BLOCK_OPERATION_AT.get();
    }

    public static class Serializer extends ActionSerializer<BlockOperationAtAction> {

        @Override
        public MapCodec<BlockOperationAtAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, BlockOperationAtAction> builder, HolderLookup.Provider provider) {
            builder.setName("Block Operation At")
                    .setDescription("Performs block operations at the entity's position.")
                    .add("block_operations", BlockOperation.documentation(provider), "Block operation(s) performed on the blocks.")
                    .addExampleObject(new BlockOperationAtAction(List.of(BlockOperation.ofBoneMeal(false))));
        }
    }

}