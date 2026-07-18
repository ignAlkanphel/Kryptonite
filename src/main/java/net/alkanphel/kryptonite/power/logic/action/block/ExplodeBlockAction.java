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

import java.util.Optional;
import java.util.function.Predicate;

public class ExplodeBlockAction extends BlockAction {

    public static final MapCodec<ExplodeBlockAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockCondition.CODEC.optionalFieldOf("destructible").forGetter(a -> a.destructibleConditions),
            BlockCondition.CODEC.optionalFieldOf("indestructible").forGetter(a -> a.indestructibleConditions),
            Level.ExplosionInteraction.CODEC.optionalFieldOf("destruction_type", Level.ExplosionInteraction.BLOCK).forGetter(a -> a.destructionType),
            Value.CODEC.optionalFieldOf("create_fire", new StaticValue(false)).forGetter(a -> a.createFire),
            Value.CODEC.fieldOf("power").forGetter(a -> a.power),
            Value.CODEC.optionalFieldOf("indestructible_resistance", new StaticValue(10)).forGetter(a -> a.indestructibleResistance)
    ).apply(instance, ExplodeBlockAction::new));

    private final Optional<BlockCondition> destructibleConditions, indestructibleConditions;
    private final Level.ExplosionInteraction destructionType;
    private final Value createFire, power, indestructibleResistance;

    public ExplodeBlockAction(Optional<BlockCondition> destructibleConditions, Optional<BlockCondition> indestructibleConditions, Level.ExplosionInteraction destructionType, Value createFire, Value power, Value indestructibleResistance) {
        this.destructibleConditions = destructibleConditions;
        this.indestructibleConditions = indestructibleConditions;
        this.destructionType = destructionType;
        this.createFire = createFire;
        this.power = power;
        this.indestructibleResistance = indestructibleResistance;
    }

    @Override
    public boolean run(BlockActionContext context) {
        ServerLevel level = context.level();
        if (level.isClientSide()) return false;

        Predicate<BlockConditionContext> behaviorCondition = ctx -> false;

        if (indestructibleConditions.isPresent()) {
            behaviorCondition = indestructibleConditions.get()::test;
        }

        if (destructibleConditions.isPresent()) {
            Predicate<BlockConditionContext> destructible = destructibleConditions.get()::test;
            behaviorCondition = MiscUtil.combineOr(destructible.negate(), behaviorCondition);
        }

        MiscUtil.createExplosion(
                level,
                context.pos().getCenter(),
                Math.max(0, power.getAsFloat(null)),
                createFire.getAsBoolean(null),
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
                    .addOptional("destructible", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, the blocks that fulfill these conditions CAN be destroyed by the explosion.")
                    .addOptional("indestructible", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, the blocks that fulfill these conditions can NOT be destroyed by the explosion.")
                    .addOptional("destruction_type", KryptoniteDocumented.TYPE_EXPLOSION_INTERACTION, "How the explosion interacts with blocks.", Level.ExplosionInteraction.BLOCK)
                    .addOptional("create_fire", TYPE_VALUE, "If the explosion creates fire (e.g. a ghast fireball).", false)
                    .add("power", TYPE_VALUE, "The strength/radius of the explosion.")
                    .addOptional("indestructible_resistance", TYPE_VALUE, "The explosion resistance value used for indestructible blocks.", 10.0F)
                    .addExampleObject(new ExplodeBlockAction(Optional.empty(), Optional.empty(), Level.ExplosionInteraction.BLOCK, new StaticValue(false), new StaticValue(4), new StaticValue(10)));
        }
    }

}