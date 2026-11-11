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
import net.threetag.palladium.logic.condition.FalseCondition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class ModifyDamageTakenAbility extends Ability {

    public static final MapCodec<ModifyDamageTakenAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KryptoniteModifiers.VALUE_MODIFIERS_CODEC.optionalFieldOf("modifiers", List.of()).forGetter(a -> a.modifiers),
            Action.LIST_CODEC.optionalFieldOf("self_actions", List.of()).forGetter(a -> a.selfActions),
            Action.LIST_CODEC.optionalFieldOf("attacker_actions", List.of()).forGetter(a -> a.attackerActions),
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            Condition.CODEC.optionalFieldOf("apply_armor_conditions", FalseCondition.INSTANCE).forGetter(a -> a.applyArmorConditions),
            Condition.CODEC.optionalFieldOf("damage_armor_conditions", FalseCondition.INSTANCE).forGetter(a -> a.damageArmorConditions),
            Condition.CODEC.optionalFieldOf("entity_conditions", TrueCondition.INSTANCE).forGetter(a -> a.entityConditions),
            DamageCondition.LIST_CODEC.optionalFieldOf("damage_conditions", List.of()).forGetter(a -> a.damageConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ModifyDamageTakenAbility::new));

    public final List<KryptoniteModifiers.ValueModifier> modifiers;
    public final List<Action> selfActions, attackerActions, entityActions;
    public final Condition applyArmorConditions, damageArmorConditions;
    public final Condition entityConditions;
    public final List<DamageCondition> damageConditions;

    public ModifyDamageTakenAbility(List<KryptoniteModifiers.ValueModifier> modifiers, List<Action> selfActions, List<Action> attackerActions, List<Action> entityActions, Condition applyArmorConditions, Condition damageArmorConditions, Condition entityConditions, List<DamageCondition> damageConditions, AbilityProperties properties, AbilityStateManager state, List<EnergyBarUsage> energyBarUsages) {
        super(properties, state, energyBarUsages);
        this.modifiers = modifiers;
        this.selfActions = selfActions;
        this.attackerActions = attackerActions;
        this.entityActions = entityActions;
        this.applyArmorConditions = applyArmorConditions;
        this.damageArmorConditions = damageArmorConditions;
        this.entityConditions = entityConditions;
        this.damageConditions = damageConditions;
    }

    public boolean modifiesArmorApplicance() {
        return this.applyArmorConditions != FalseCondition.INSTANCE;
    }

    public boolean shouldApplyArmor(LivingEntity self) {
        return applyArmorConditions.test(DataContext.forEntity(self));
    }

    public boolean modifiesArmorDamaging() {
        return this.damageArmorConditions != FalseCondition.INSTANCE;
    }

    public boolean shouldDamageArmor(LivingEntity self) {
        return damageArmorConditions.test(DataContext.forEntity(self));
    }

    public boolean doesApply(DamageSource source, float damageAmount, Entity self, AbilityInstance<?> instance) {
        Entity attacker = source.getEntity();

        return DamageCondition.checkConditions(damageConditions, source, damageAmount)
                && (attacker == null || entityConditions.test(DataContext.forAffected(self, attacker, instance)));
    }

    public void runActions(Entity attacker, Entity self, AbilityInstance<?> instance) {
        if (!selfActions.isEmpty()) Action.runList(selfActions, DataContext.forAffected(self, attacker, instance));
        if (attacker != null && !attackerActions.isEmpty()) Action.runList(attackerActions, DataContext.forAffected(attacker, self, instance));
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forAffected(self, attacker, instance));
    }

    public float applyModifiers(float damage, AbilityInstance<?> instance, LivingEntity entity) {
        if (modifiers.isEmpty()) return damage;

        return Math.max(0F, KryptoniteModifiers.applyModifiers(damage, modifiers, DataContext.forAbility(entity, instance)));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.MODIFY_DAMAGE_TAKEN.get();
    }

    public static class Serializer extends AbilitySerializer<ModifyDamageTakenAbility> {

        @Override
        public MapCodec<ModifyDamageTakenAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ModifyDamageTakenAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Modify Damage Taken")
                    .setDescription("Modifies the amount of damage the entity that has this ability takes. For this ability, the \"source\" is the entity that dealt the damage & the \"target\" is the entity that took the damage.")
                    .add("modifiers", KryptoniteDocumented.TYPE_VALUE_MODIFIER, "Modifier(s) applied to the damage taken by the target.")
                    .addOptional("self_actions", TYPE_ACTION_LIST, "Action(s) performed on the target.")
                    .addOptional("attacker_actions", TYPE_ACTION_LIST, "Action(s) performed on the source.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "Action(s) performed on the target, with the source available in the context.")
                    .addOptional("apply_armor_conditions", TYPE_CONDITION_LIST, "Condition(s) the target must meet for armor to be applied to the damage.")
                    .addOptional("damage_armor_conditions", TYPE_CONDITION_LIST, "Condition(s) the target must meet for its armor to be damaged.")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) evaluated with the source and target available in the context.")
                    .addOptional("damage_conditions", KryptoniteDocumented.TYPE_DAMAGE_CONDITION_LIST, "Damage condition(s) the incoming damage must meet for the modifiers and actions to apply.")
                    .addExampleObject(new ModifyDamageTakenAbility(List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(-0.5), KryptoniteModifiers.Operation.MULTIPLY_BASE_ADDITIVE)), List.of(), List.of(), List.of(), FalseCondition.INSTANCE, FalseCondition.INSTANCE, TrueCondition.INSTANCE, List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}