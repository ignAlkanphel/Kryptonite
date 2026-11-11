package net.alkanphel.kryptonite.power.logic.action.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializers;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.context.BlockActionContext;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.alkanphel.kryptonite.util.apoli.MiscUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.logic.value.holder.BooleanValueHolder;

import java.util.List;
import java.util.function.Predicate;

public class ExplodeBlockAction extends BlockAction {

    public static final MapCodec<ExplodeBlockAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockCondition.LIST_CODEC.optionalFieldOf("destructible", List.of()).forGetter(a -> a.destructibleConditions),
            BlockCondition.LIST_CODEC.optionalFieldOf("indestructible", List.of()).forGetter(a -> a.indestructibleConditions),
            Level.ExplosionInteraction.CODEC.optionalFieldOf("destruction_type", Level.ExplosionInteraction.BLOCK).forGetter(a -> a.destructionType),
            BooleanValueHolder.CODEC.optionalFieldOf("create_fire", BooleanValueHolder.FALSE).forGetter(a -> a.createFire),
            Value.NUMBER_CODEC.fieldOf("power").forGetter(a -> a.power),
            Value.NUMBER_CODEC.optionalFieldOf("indestructible_resistance", new StaticValue(10)).forGetter(a -> a.indestructibleResistance)
    ).apply(instance, ExplodeBlockAction::new));

    private final List<BlockCondition> destructibleConditions, indestructibleConditions;
    private final Level.ExplosionInteraction destructionType;
    private final BooleanValueHolder createFire;
    private final Value power, indestructibleResistance;

    public ExplodeBlockAction(List<BlockCondition> destructibleConditions, List<BlockCondition> indestructibleConditions, Level.ExplosionInteraction destructionType, BooleanValueHolder createFire, Value power, Value indestructibleResistance) {
        this.destructibleConditions = destructibleConditions;
        this.indestructibleConditions = indestructibleConditions;
        this.destructionType = destructionType;
        this.createFire = createFire;
        this.power = power;
        this.indestructibleResistance = indestructibleResistance;
    }

    @Override
    public boolean execute(BlockActionContext context) {
        ServerLevel level = context.level();
        if (level.isClientSide()) return false;

        Predicate<BlockConditionContext> behaviorCondition = ctx -> false;

        if (!indestructibleConditions.isEmpty()) {
            behaviorCondition = ctx -> BlockCondition.checkConditions(indestructibleConditions, ctx);
        }

        if (!destructibleConditions.isEmpty()) {
            Predicate<BlockConditionContext> destructible = ctx -> BlockCondition.checkConditions(destructibleConditions, ctx);
            behaviorCondition = MiscUtil.combineOr(destructible.negate(), behaviorCondition);
        }

        MiscUtil.createExplosion(
                level,
                context.pos().getCenter(),
                Math.max(0, power.getAsFloat(null)),
                createFire.get(null),
                destructionType,
                MiscUtil.createExplosionDamageCalculator(behaviorCondition, Math.max(0, indestructibleResistance.getAsFloat(null)))
        );

        return true;
    }

    @Override
    public BlockActionSerializer<?> getSerializer() {
        return BlockActionSerializers.EXPLODE.get();
    }

    public static class Serializer extends BlockActionSerializer<ExplodeBlockAction> {

        @Override
        public MapCodec<ExplodeBlockAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockAction, ExplodeBlockAction> builder, HolderLookup.Provider provider) {
            builder.setName("Explode")
                    .setDescription("Creates an explosion at the block's position.")
                    .addOptional("destructible", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "Block condition(s) the blocks in the explosion must meet to be destroyed.")
                    .addOptional("indestructible", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "Block condition(s) the blocks in the explosion must meet to not be affected.")
                    .addOptional("destruction_type", KryptoniteDocumented.TYPE_EXPLOSION_INTERACTION, "How the explosion interacts with blocks.", Level.ExplosionInteraction.BLOCK)
                    .addOptional("create_fire", TYPE_BOOLEAN_VALUE, "If true, the explosion will create fire (e.g. ghast fireball).", false)
                    .add("power", TYPE_FLOAT_VALUE, "The strength/radius of the explosion.")
                    .addOptional("indestructible_resistance", TYPE_FLOAT_VALUE, "The explosion resistance value used for indestructible blocks.", 10.0F)
                    .addExampleObject(new ExplodeBlockAction(List.of(), List.of(), Level.ExplosionInteraction.BLOCK, BooleanValueHolder.FALSE, new StaticValue(4), new StaticValue(10)));
        }
    }

}