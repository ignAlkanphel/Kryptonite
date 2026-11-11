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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.logic.value.holder.BooleanValueHolder;

import java.util.List;
import java.util.function.Predicate;

public class ExplodeAction extends Action {

    public static final MapCodec<ExplodeAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockCondition.LIST_CODEC.optionalFieldOf("destructible", List.of()).forGetter(ab -> ab.destructibleConditions),
            BlockCondition.LIST_CODEC.optionalFieldOf("indestructible", List.of()).forGetter(ab -> ab.indestructibleConditions),
            Level.ExplosionInteraction.CODEC.optionalFieldOf("destruction_type", Level.ExplosionInteraction.BLOCK).forGetter(a -> a.destructionType),
            Condition.CODEC.optionalFieldOf("damage_entity_conditions", TrueCondition.INSTANCE).forGetter(a -> a.damageEntityConditions),
            BooleanValueHolder.CODEC.optionalFieldOf("damage_self", BooleanValueHolder.TRUE).forGetter(a -> a.damageSelf),
            BooleanValueHolder.CODEC.optionalFieldOf("create_fire", BooleanValueHolder.FALSE).forGetter(a -> a.createFire),
            Value.NUMBER_CODEC.fieldOf("power").forGetter(a -> a.power),
            Value.NUMBER_CODEC.optionalFieldOf("indestructible_resistance", new StaticValue(10)).forGetter(a -> a.indestructibleResistance)
    ).apply(instance, ExplodeAction::new));

    public final List<BlockCondition> destructibleConditions, indestructibleConditions;
    public final Level.ExplosionInteraction destructionType;
    public final Condition damageEntityConditions;
    public final BooleanValueHolder damageSelf, createFire;
    public final Value power, indestructibleResistance;

    public ExplodeAction(List<BlockCondition> destructibleConditions, List<BlockCondition> indestructibleConditions, Level.ExplosionInteraction destructionType, Condition damageEntityConditions, BooleanValueHolder damageSelf, BooleanValueHolder createFire, Value power, Value indestructibleResistance) {
        this.destructibleConditions = destructibleConditions;
        this.indestructibleConditions = indestructibleConditions;
        this.destructionType = destructionType;
        this.damageEntityConditions = damageEntityConditions;
        this.damageSelf = damageSelf;
        this.createFire = createFire;
        this.power = power;
        this.indestructibleResistance = indestructibleResistance;
    }

    @Override
    protected boolean execute(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        var level = entity.level();
        if (level.isClientSide()) return false;

        Predicate<BlockConditionContext> behaviorCondition = ctx -> false;

        if (!indestructibleConditions.isEmpty()) {
            behaviorCondition = ctx -> BlockCondition.checkConditions(indestructibleConditions, ctx);
        }

        if (!destructibleConditions.isEmpty()) {
            Predicate<BlockConditionContext> destructible = ctx -> BlockCondition.checkConditions(destructibleConditions, ctx);
            behaviorCondition = MiscUtil.combineOr(destructible.negate(), behaviorCondition);
        }

        DamageSource damageSource = Explosion.getDefaultDamageSource(level, entity);

        boolean damageSelf = this.damageSelf.get(context);

        Predicate<Entity> damageEntityCondition = target -> {
            if (target == entity) return damageSelf;
            return damageEntityConditions.test(DataContext.forAffected(target, entity, context.getAbility()));
        };

        MiscUtil.createExplosion(
                level,
                damageSelf ? null : entity,
                damageSource,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                Math.max(0, power.getAsFloat(context)),
                createFire.get(context),
                destructionType,
                MiscUtil.createExplosionDamageCalculator(behaviorCondition, Math.max(0, indestructibleResistance.getAsFloat(context)), damageEntityCondition)
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
                    .addOptional("destructible", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "Block condition(s) the blocks in the explosion must meet to be destroyed.")
                    .addOptional("indestructible", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "Block condition(s) the blocks in the explosion must meet to not be affected.")
                    .addOptional("destruction_type", KryptoniteDocumented.TYPE_EXPLOSION_INTERACTION, "How the explosion interacts with blocks.", Level.ExplosionInteraction.BLOCK)
                    .addOptional("damage_entity_conditions", TYPE_CONDITION_LIST, "Condition(s) an entity must meet to take damage from the explosion.")
                    .addOptional("damage_self", TYPE_BOOLEAN_VALUE, "If true, the entity that triggered the explosion will also take damage from it.", true)
                    .addOptional("create_fire", TYPE_BOOLEAN_VALUE, "If true, the explosion will create fire (e.g. ghast fireball).", false)
                    .addOptional("power", KryptoniteSettingType.floatValueRange(0, Integer.MAX_VALUE), "The strength/radius of the explosion.")
                    .addOptional("indestructible_resistance", KryptoniteSettingType.floatValueRange(0, Integer.MAX_VALUE), "The explosion resistance value used for indestructible blocks.", 10.0F)
                    .addExampleObject(new ExplodeAction(List.of(), List.of(), Level.ExplosionInteraction.BLOCK, TrueCondition.INSTANCE, BooleanValueHolder.FALSE, BooleanValueHolder.FALSE, new StaticValue(4), new StaticValue(10)));
        }
    }

}