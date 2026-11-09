package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.AddVelocityAction;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageCondition;
import net.alkanphel.kryptonite.util.KryptoniteCodecs;
import net.alkanphel.kryptonite.util.apoli.Space;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.AsSourceAction;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.HealthCondition;
import net.threetag.palladium.logic.condition.NumberRange;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class ActionOnHitAbility extends Ability {

    public static final MapCodec<ActionOnHitAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            Condition.CODEC.optionalFieldOf("entity_conditions", TrueCondition.INSTANCE).forGetter(a -> a.entityConditions),
            DamageCondition.LIST_CODEC.optionalFieldOf("damage_conditions", List.of()).forGetter(a -> a.damageConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnHitAbility::new));

    public final List<Action> entityActions;
    public final Condition entityConditions;
    public final List<DamageCondition> damageConditions;

    public ActionOnHitAbility(List<Action> entityActions, Condition entityConditions, List<DamageCondition> damageConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.entityConditions = entityConditions;
        this.damageConditions = damageConditions;
    }

    public boolean doesApply(LivingEntity source, Entity target, DamageSource damageSource, float amount, AbilityInstance<?> instance) {
        if (!damageConditions.isEmpty() && !DamageCondition.checkConditions(damageConditions, damageSource, amount)) {
            return false;
        }

        return entityConditions.test(DataContext.forAffected(target, source, instance));
    }

    public void onHit(LivingEntity source, Entity target, AbilityInstance<?> instance) {
        if (!entityActions.isEmpty()) {
            Action.runList(entityActions, DataContext.forAffected(target, source, instance));
        }
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_HIT.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnHitAbility> {

        @Override
        public MapCodec<ActionOnHitAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnHitAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action On Hit")
                    .setDescription("Performs actions when the entity that has this ability hits another entity. For this ability, the \"source\" is the entity that has this ability & the \"target\" is the entity being hit.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "Action(s) performed on the target.")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) that the target must meet for the actions to perform.")
                    .addOptional("damage_conditions", KryptoniteDocumented.TYPE_DAMAGE_CONDITION_LIST, "Damage condition(s) that the hit must meet for the actions to perform.")
                    .addExampleObject(new ActionOnHitAbility(List.of(new RunCommandAction(new ParsedCommands("say I was hit!"))), TrueCondition.INSTANCE, List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnHitAbility(List.of(new AsSourceAction(List.of(new RunCommandAction(new ParsedCommands("say I hit something!"))))), TrueCondition.INSTANCE, List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnHitAbility(List.of(new AsSourceAction(List.of(new AddVelocityAction(new KryptoniteCodecs.Vec3fValue(new StaticValue(0F), new StaticValue(0F), new StaticValue(0.5F)), Space.LOCAL_HORIZONTAL_NORMALIZED, new StaticValue(true))))), TrueCondition.INSTANCE, List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnHitAbility(List.of(new AsSourceAction(List.of(new AddVelocityAction(new KryptoniteCodecs.Vec3fValue(new StaticValue(0F), new StaticValue(-0.1F), new StaticValue(-0.5F)), Space.LOCAL, new StaticValue(true))))), TrueCondition.INSTANCE, List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnHitAbility(List.of(new RunCommandAction(new ParsedCommands("execute as @s at @s run summon minecraft:zombie"))), new HealthCondition(new NumberRange(0F, 0F)), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }
}