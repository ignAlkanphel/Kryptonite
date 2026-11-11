package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageCondition;
import net.alkanphel.kryptonite.util.KryptoniteModifiers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;
import java.util.Optional;

public class ModifyDamageDealtAbility extends Ability {

    public static final MapCodec<ModifyDamageDealtAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KryptoniteModifiers.VALUE_MODIFIERS_CODEC.optionalFieldOf("modifiers", List.of()).forGetter(a -> a.modifiers),
            Action.LIST_CODEC.optionalFieldOf("self_actions", List.of()).forGetter(a -> a.selfActions),
            Action.LIST_CODEC.optionalFieldOf("target_actions", List.of()).forGetter(a -> a.targetActions),
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            Condition.CODEC.optionalFieldOf("target_conditions").forGetter(a -> a.targetConditions),
            Condition.CODEC.optionalFieldOf("entity_conditions").forGetter(a -> a.entityConditions),
            DamageCondition.CODEC.optionalFieldOf("damage_conditions").forGetter(a -> a.damageConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ModifyDamageDealtAbility::new));

    public final List<KryptoniteModifiers.ValueModifier> modifiers;
    public final List<Action> selfActions, targetActions, entityActions;
    public final Optional<Condition> targetConditions, entityConditions;
    public final Optional<DamageCondition> damageConditions;

    public ModifyDamageDealtAbility(List<KryptoniteModifiers.ValueModifier> modifiers, List<Action> selfActions, List<Action> targetActions, List<Action> entityActions, Optional<Condition> targetConditions, Optional<Condition> entityConditions, Optional<DamageCondition> damageConditions, AbilityProperties properties, AbilityStateManager state, List<EnergyBarUsage> energyBarUsages) {
        super(properties, state, energyBarUsages);
        this.modifiers = modifiers;
        this.selfActions = selfActions;
        this.targetActions = targetActions;
        this.entityActions = entityActions;
        this.targetConditions = targetConditions;
        this.entityConditions = entityConditions;
        this.damageConditions = damageConditions;
    }

    public boolean doesApply(DamageSource source, float damageAmount, Entity attacker, Entity target, AbilityInstance<?> instance) {
        return damageConditions.map(condition -> condition.test(source, damageAmount)).orElse(true)
                && targetConditions.map(condition -> condition.test(DataContext.forAffected(target, attacker, instance))).orElse(true)
                && entityConditions.map(condition -> condition.test(DataContext.forAffected(target, attacker, instance))).orElse(true);
    }

    public void runActions(Entity attacker, Entity target, AbilityInstance<?> instance) {
        if (!selfActions.isEmpty()) Action.runList(selfActions, DataContext.forAffected(attacker, target, instance));
        if (!targetActions.isEmpty()) Action.runList(targetActions, DataContext.forAffected(target, attacker, instance));
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forAffected(target, attacker, instance));
    }

    public float applyModifiers(float damage, AbilityInstance<?> instance, LivingEntity entity) {
        if (modifiers.isEmpty()) return damage;

        return Math.max(0F, KryptoniteModifiers.applyModifiers(damage, modifiers, DataContext.forAbility(entity, instance)));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.MODIFY_DAMAGE_DEALT.get();
    }

    public static class Serializer extends AbilitySerializer<ModifyDamageDealtAbility> {

        @Override
        public MapCodec<ModifyDamageDealtAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ModifyDamageDealtAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Modify Damage Dealt")
                    .setDescription("Modifies the amount of damage the entity that has this ability deals. For this ability, the \"source\" is the entity that dealt the damage & the \"target\" is the entity that was hit.")
                    .add("modifiers", KryptoniteDocumented.TYPE_VALUE_MODIFIER, "Modifier(s) applied to the damage dealt by the \"source\" entity.")
                    .addOptional("self_actions", TYPE_ACTION_LIST, "Action(s) performed on the source.")
                    .addOptional("target_actions", TYPE_ACTION_LIST, "Action(s) performed on the target.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "Action(s) performed on the target, with the source available in the context.")
                    .addOptional("target_conditions", TYPE_CONDITION_LIST, "Condition(s) the target must meet for the modifiers and actions to apply.")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) evaluated with the source and target available in the context.")
                    .addOptional("damage_conditions", KryptoniteDocumented.TYPE_DAMAGE_CONDITION_LIST, "Damage condition(s) the dealt damage must meet for the modifiers and actions to apply.")
                    .addExampleObject(new ModifyDamageDealtAbility(List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(4.0), KryptoniteModifiers.Operation.ADD_BASE_EARLY)), List.of(), List.of(), List.of(), Optional.empty(), Optional.empty(), Optional.empty(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}