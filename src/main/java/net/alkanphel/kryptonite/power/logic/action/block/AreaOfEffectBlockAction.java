package net.alkanphel.kryptonite.power.logic.action.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializers;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.context.BlockActionContext;
import net.alkanphel.kryptonite.util.apoli.Shape;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

import java.util.List;

public class AreaOfEffectBlockAction extends BlockAction {

    public static final MapCodec<AreaOfEffectBlockAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockAction.LIST_CODEC.fieldOf("block_actions").forGetter(a -> a.blockActions),
            BlockCondition.LIST_CODEC.optionalFieldOf("block_conditions", List.of()).forGetter(a -> a.blockConditions),
            Shape.CODEC.optionalFieldOf("shape", Shape.CUBE).forGetter(a -> a.shape),
            Value.NUMBER_CODEC.optionalFieldOf("radius", new StaticValue(16)).forGetter(a -> a.radius)
    ).apply(instance, AreaOfEffectBlockAction::new));

    private final List<BlockAction> blockActions;
    private final List<BlockCondition> blockConditions;
    private final Shape shape;
    private final Value radius;

    public AreaOfEffectBlockAction(List<BlockAction> blockActions, List<BlockCondition> blockConditions, Shape shape, Value radius) {
        this.blockActions = blockActions;
        this.blockConditions = blockConditions;
        this.shape = shape;
        this.radius = radius;
    }

    @Override
    public boolean execute(BlockActionContext context) {
        var level = context.level();

        shape.getBlockPositions(context.pos(), radius.getAsInt(null))
                .stream()
                .filter(pos -> BlockCondition.checkConditions(blockConditions, level, pos))
                .forEach(pos -> BlockAction.runList(blockActions, level, pos, context.direction()));

        return true;
    }

    @Override
    public BlockActionSerializer<?> getSerializer() {
        return BlockActionSerializers.AREA_OF_EFFECT.get();
    }

    public static class Serializer extends BlockActionSerializer<AreaOfEffectBlockAction> {

        @Override
        public MapCodec<AreaOfEffectBlockAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockAction, AreaOfEffectBlockAction> builder, HolderLookup.Provider provider) {
            builder.setName("Area of Effect")
                    .setDescription("Performs block actions on all blocks within a radius.")
                    .add("block_actions", KryptoniteDocumented.TYPE_BLOCK_ACTION_LIST, "Block action(s) performed on the blocks in range.")
                    .addOptional("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "Block condition(s) the blocks in range must meet for the actions to perform.")
                    .addOptional("shape", KryptoniteDocumented.TYPE_SHAPE, "The shape of the area.", Shape.CUBE)
                    .addOptional("radius", TYPE_INT_VALUE, "The radius of the area.", 16)
                    .addExampleObject(new AreaOfEffectBlockAction(List.of(), List.of(), Shape.CUBE, new StaticValue(16)));
        }
    }

}