package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.meta.ActorConditionBiCondition;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageCondition;
import net.alkanphel.kryptonite.util.KryptoniteModifiers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.entity.PlayerSlot;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ItemInSlotCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;
import java.util.Optional;

public class ModifyDamageTakenAbility extends Ability {

    public static final MapCodec<ModifyDamageTakenAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KryptoniteModifiers.VALUE_MODIFIERS_CODEC.optionalFieldOf("modifiers", List.of()).forGetter(a -> a.modifiers),
            Action.LIST_CODEC.optionalFieldOf("self_actions", List.of()).forGetter(a -> a.selfAction),
            Action.LIST_CODEC.optionalFieldOf("attacker_actions", List.of()).forGetter(a -> a.attackerAction),
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions", List.of()).forGetter(a -> a.biEntityAction),
            Condition.CODEC.optionalFieldOf("apply_armor_conditions").forGetter(a -> a.applyArmorCondition),
            Condition.CODEC.optionalFieldOf("damage_armor_conditions").forGetter(a -> a.damageArmorCondition),
            BiCondition.CODEC.optionalFieldOf("bientity_conditions").forGetter(a -> a.biEntityCondition),
            DamageCondition.CODEC.optionalFieldOf("damage_conditions").forGetter(a -> a.damageCondition),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ModifyDamageTakenAbility::new));

    public final List<KryptoniteModifiers.ValueModifier> modifiers;
    public final List<Action> selfAction, attackerAction;
    public final List<BiAction> biEntityAction;
    public final Optional<Condition> applyArmorCondition, damageArmorCondition;
    public final Optional<BiCondition> biEntityCondition;
    public final Optional<DamageCondition> damageCondition;

    public ModifyDamageTakenAbility(List<KryptoniteModifiers.ValueModifier> modifiers, List<Action> selfAction, List<Action> attackerAction, List<BiAction> biEntityAction, Optional<Condition> applyArmorCondition, Optional<Condition> damageArmorCondition, Optional<BiCondition> biEntityCondition, Optional<DamageCondition> damageCondition, AbilityProperties properties, AbilityStateManager state, List<EnergyBarUsage> energyBarUsages) {
        super(properties, state, energyBarUsages);
        this.modifiers = modifiers;
        this.selfAction = selfAction;
        this.attackerAction = attackerAction;
        this.biEntityAction = biEntityAction;
        this.applyArmorCondition = applyArmorCondition;
        this.damageArmorCondition = damageArmorCondition;
        this.biEntityCondition = biEntityCondition;
        this.damageCondition = damageCondition;
    }

    public boolean modifiesArmorApplicance() {
        return this.applyArmorCondition.isPresent();
    }

    public boolean shouldApplyArmor(Entity self) {
        return applyArmorCondition
                .map(condition -> condition.test(DataContext.forEntity(self)))
                .orElse(false);
    }

    public boolean modifiesArmorDamaging() {
        return this.damageArmorCondition.isPresent();
    }

    public boolean shouldDamageArmor(LivingEntity self) {
        return damageArmorCondition
                .map(condition -> condition.test(DataContext.forEntity(self)))
                .orElse(false);
    }

    public boolean doesApply(DamageSource source, float damageAmount, Entity self) {
        Entity attacker = source.getEntity();
        return attacker == null
                ? damageCondition.map(condition -> condition.test(source, damageAmount)).orElse(true)
                && biEntityCondition.isEmpty()
                : damageCondition.map(condition -> condition.test(source, damageAmount)).orElse(true)
                && biEntityCondition.map(condition -> condition.test(attacker, self)).orElse(true);
    }

    public void runActions(Entity attacker, Entity self) {
        if (!selfAction.isEmpty()) Action.runList(selfAction, DataContext.forEntity(self));
        if (!attackerAction.isEmpty()) Action.runList(attackerAction, DataContext.forEntity(self));
        if (!biEntityAction.isEmpty()) BiAction.runList(biEntityAction, attacker, self);
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
                    .setDescription("Modifies the amount of damage the entity that has this ability takes. In the context of this ability, the \"actor\" is the entity that did the attacking & the \"target\" entity is the entity that has this ability.")
                    .add("modifiers", KryptoniteDocumented.TYPE_VALUE_MODIFIER, "If specified, these modifiers will be applied to the damage taken by the \"target\" entity.")
                    .addOptional("self_actions", TYPE_ACTION_LIST, "If specified, these actions will be run on the \"target\" entity whenever the modifiers apply.")
                    .addOptional("attacker_actions", TYPE_ACTION_LIST, "If specified, these actions will be run on the \"actor\" entity whenever the modifiers apply.")
                    .addOptional("bientity_actions", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "If specified, these actions will be run on either or both \"actor\" & \"target\" entities whenever the modifiers apply.")
                    .addOptional("apply_armor_conditions", TYPE_CONDITION_LIST, "If specified, armor will only be applied to the damage taken if these conditions are fulfilled by the \"target\" entity.")
                    .addOptional("damage_armor_conditions", TYPE_CONDITION_LIST, "If specified, worn armor will only be damaged if these conditions are fulfilled by the \"target\" entity.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, the specified actions/modifiers will only be run/applied if these conditions are fulfilled by either or both \"actor\" & \"target\" entities.")
                    .addOptional("damage_conditions", KryptoniteDocumented.TYPE_DAMAGE_CONDITION_LIST, "If specified, the specified modifiers and/or actions will only apply if the taken damage fulfills these conditions.")
                    .addExampleObject(new ModifyDamageTakenAbility(List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(-0.5), KryptoniteModifiers.Operation.MULTIPLY_BASE_ADDITIVE)), List.of(), List.of(), List.of(), Optional.empty(), Optional.empty(), Optional.of(new ActorConditionBiCondition(new ItemInSlotCondition(Ingredient.of(Items.GOLDEN_SWORD), PlayerSlot.get(EquipmentSlot.MAINHAND)))), Optional.empty(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}