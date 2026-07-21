package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.KryptoniteSettingType;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.alkanphel.kryptonite.util.apoli.MiscUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

import java.util.Optional;
import java.util.function.Predicate;

public class ExplodeAction extends Action {

    public static final MapCodec<ExplodeAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockCondition.CODEC.optionalFieldOf("destructible").forGetter(ab -> ab.destructibleConditions),
            BlockCondition.CODEC.optionalFieldOf("indestructible").forGetter(ab -> ab.indestructibleConditions),
            Level.ExplosionInteraction.CODEC.optionalFieldOf("destruction_type", Level.ExplosionInteraction.BLOCK).forGetter(a -> a.destructionType),
            Value.CODEC.optionalFieldOf("damage_self", new StaticValue(true)).forGetter(a -> a.damageSelf),
            Value.CODEC.optionalFieldOf("create_fire", new StaticValue(false)).forGetter(a -> a.createFire),
            Value.CODEC.fieldOf("power").forGetter(a -> a.power),
            Value.CODEC.optionalFieldOf("indestructible_resistance", new StaticValue(10)).forGetter(a -> a.indestructibleResistance)
    ).apply(instance, ExplodeAction::new));

    public final Optional<BlockCondition> destructibleConditions, indestructibleConditions;
    public final Level.ExplosionInteraction destructionType;
    public final Value damageSelf;
    public final Value createFire, power, indestructibleResistance;

    public ExplodeAction(Optional<BlockCondition> destructibleConditions, Optional<BlockCondition> indestructibleConditions, Level.ExplosionInteraction destructionType, Value damageSelf, Value createFire, Value power, Value indestructibleResistance) {
        this.destructibleConditions = destructibleConditions;
        this.indestructibleConditions = indestructibleConditions;
        this.destructionType = destructionType;
        this.damageSelf = damageSelf;
        this.createFire = createFire;
        this.power = power;
        this.indestructibleResistance = indestructibleResistance;
    }

    @Override
    public boolean run(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        var level = entity.level();
        if (level.isClientSide()) return false;

        Predicate<BlockConditionContext> behaviorCondition = ctx -> false;

        if (indestructibleConditions.isPresent()) {
            behaviorCondition = indestructibleConditions.get()::test;
        }

        if (destructibleConditions.isPresent()) {
            Predicate<BlockConditionContext> destructible = destructibleConditions.get()::test;
            behaviorCondition = MiscUtil.combineOr(destructible.negate(), behaviorCondition);
        }

        DamageSource damageSource = Explosion.getDefaultDamageSource(level, entity);

        MiscUtil.createExplosion(
                level,
                damageSelf.getAsBoolean(context) ? null : entity,
                damageSource,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                Math.max(0, power.getAsFloat(context)),
                createFire.getAsBoolean(context),
                destructionType,
                MiscUtil.createExplosionDamageCalculator(behaviorCondition, Math.max(0, indestructibleResistance.getAsFloat(context)))
        );

        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.EXPLODE.get();
    }

    public static class Serializer extends ActionSerializer<ExplodeAction> {

        @Override
        public MapCodec<ExplodeAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, ExplodeAction> builder, HolderLookup.Provider provider) {
            builder.setName("Explode")
                    .setDescription("Creates an explosion.")
                    .addOptional("destructible", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, the blocks that fulfill these conditions CAN be destroyed by the explosion.")
                    .addOptional("indestructible", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, the blocks fulfill the conditions can NOT be destroyed by the explosion.")
                    .addOptional("destruction_type", KryptoniteDocumented.TYPE_EXPLOSION_INTERACTION, "How the explosion interacts with blocks.", Level.ExplosionInteraction.BLOCK)
                    .addOptional("damage_self", TYPE_VALUE, "If the entity that triggered the explosion also takes damage from it.", true)
                    .addOptional("create_fire", TYPE_VALUE, "If the explosion creates fire (e.g. a ghast fireball).", false)
                    .addOptional("power", KryptoniteSettingType.floatValueRange(0, Integer.MAX_VALUE), "The strength/radius of the explosion.")
                    .addOptional("indestructible_resistance", KryptoniteSettingType.floatValueRange(0, Integer.MAX_VALUE), "The explosion resistance value used for indestructible blocks.", 10.0F)
                    .addExampleObject(new ExplodeAction(Optional.empty(), Optional.empty(), Level.ExplosionInteraction.BLOCK, new StaticValue(false), new StaticValue(false), new StaticValue(4), new StaticValue(10)));
        }
    }

}