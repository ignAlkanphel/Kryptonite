package net.alkanphel.kryptonite.power.logic.action.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockActionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

import java.util.List;

public class OffsetBlockAction extends BlockAction {

    public static final MapCodec<OffsetBlockAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockAction.LIST_CODEC.fieldOf("block_actions").forGetter(a -> a.blockActions),
            Value.CODEC.optionalFieldOf("x", new StaticValue(0)).forGetter(a -> a.x),
            Value.CODEC.optionalFieldOf("y", new StaticValue(0)).forGetter(a -> a.y),
            Value.CODEC.optionalFieldOf("z", new StaticValue(0)).forGetter(a -> a.z)
    ).apply(instance, OffsetBlockAction::new));

    private final List<BlockAction> blockActions;
    private final Value x, y, z;

    public OffsetBlockAction(List<BlockAction> blockActions, Value x, Value y, Value z) {
        this.blockActions = blockActions;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean run(BlockActionContext context) {
        BlockPos offsetPos = context.pos().offset(x.getAsInt(null), y.getAsInt(null), z.getAsInt(null));
        BlockAction.runList(blockActions, context.level(), offsetPos, context.direction());
        return true;
    }

    @Override
    public BlockActionSerializer<?> getSerializer() {
        return BlockActionSerializers.OFFSET.get();
    }

    public static class Serializer extends BlockActionSerializer<OffsetBlockAction> {

        @Override
        public MapCodec<OffsetBlockAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockAction, OffsetBlockAction> builder, HolderLookup.Provider provider) {
            builder.setName("Offset")
                    .setDescription("Runs the block actions at an offset position relative to the original block.")
                    .add("block_actions", KryptoniteDocumented.TYPE_BLOCK_ACTION_LIST, "The block actions to run at the offset position.")
                    .addOptional("x", TYPE_VALUE, "How much to offset the position on the x-axis.", 0)
                    .addOptional("y", TYPE_VALUE, "How much to offset the position on the y-axis.", 0)
                    .addOptional("z", TYPE_VALUE, "How much to offset the position on the z-axis.", 0)
                    .addExampleObject(new OffsetBlockAction(List.of(), new StaticValue(0), new StaticValue(1), new StaticValue(0)));
        }
    }

}