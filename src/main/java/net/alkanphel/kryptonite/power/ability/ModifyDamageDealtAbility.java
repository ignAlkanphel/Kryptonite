package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.condition.bi.OwnerBiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
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
            Action.LIST_CODEC.optionalFieldOf("self_actions", List.of()).forGetter(a -> a.selfAction),
            Action.LIST_CODEC.optionalFieldOf("target_actions", List.of()).forGetter(a -> a.targetAction),
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions", List.of()).forGetter(a -> a.biEntityAction),
            Condition.CODEC.optionalFieldOf("target_conditions").forGetter(a -> a.targetCondition),
            BiCondition.CODEC.optionalFieldOf("bientity_conditions").forGetter(a -> a.biEntityCondition),
            DamageCondition.CODEC.optionalFieldOf("damage_conditions").forGetter(a -> a.damageCondition),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ModifyDamageDealtAbility::new));

    public final List<KryptoniteModifiers.ValueModifier> modifiers;
    public final List<Action> selfAction, targetAction;
    public final List<BiAction> biEntityAction;
    public final Optional<Condition> targetCondition;
    public final Optional<BiCondition> biEntityCondition;
    public final Optional<DamageCondition> damageCondition;

    public ModifyDamageDealtAbility(List<KryptoniteModifiers.ValueModifier> modifiers, List<Action> selfAction, List<Action> targetAction, List<BiAction> biEntityAction, Optional<Condition> targetCondition, Optional<BiCondition> biEntityCondition, Optional<DamageCondition> damageCondition, AbilityProperties properties, AbilityStateManager state, List<EnergyBarUsage> energyBarUsages) {
        super(properties, state, energyBarUsages);
        this.modifiers = modifiers;
        this.selfAction = selfAction;
        this.targetAction = targetAction;
        this.biEntityAction = biEntityAction;
        this.targetCondition = targetCondition;
        this.biEntityCondition = biEntityCondition;
        this.damageCondition = damageCondition;
    }

    public boolean doesApply(DamageSource source, float damageAmount, Entity attacker, Entity target) {
        return damageCondition.map(condition -> condition.test(source, damageAmount)).orElse(true)
                && targetCondition.map(condition -> condition.test(DataContext.forEntity(target))).orElse(true)
                && biEntityCondition.map(condition -> condition.test(attacker, target)).orElse(true);
    }

    public void runActions(Entity attacker, Entity target) {
        if (!selfAction.isEmpty()) Action.runList(selfAction, DataContext.forEntity(attacker));
        if (!targetAction.isEmpty()) Action.runList(targetAction, DataContext.forEntity(target));
        if (!biEntityAction.isEmpty()) BiAction.runList(biEntityAction, attacker, target);
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
                    .setDescription("Modifies the amount of damage the entity that has this ability deals. In the context of this ability, the \"actor\" is the entity that has this ability & the \"target\" is the entity that was hit.")
                    .add("modifiers", TYPE_ATTRIBUTE_MODIFIER, "If specified, these modifiers will be applied to the damage dealt by the \"actor\" entity.")
                    .addOptional("self_actions", TYPE_ACTION_LIST, "If specified, these actions will be run on the \"actor\" entity whenever the modifiers apply.")
                    .addOptional("target_actions", TYPE_ACTION_LIST, "If specified, these actions will be run on the \"target\" entity whenever the modifiers apply.")
                    .addOptional("bientity_actions", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "If specified, these actions will be run on either or both \"actor\" & \"target\" entities whenever the modifiers apply.")
                    .addOptional("target_conditions", TYPE_CONDITION_LIST, "If specified, the specified actions/modifiers will only be run/applied if these conditions are fulfilled by the \"target\" entity.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, the specified actions/modifiers will only be run/applied if these conditions are fulfilled by either or both \"actor\" & \"target\" entities.")
                    .addOptional("damage_conditions", KryptoniteDocumented.TYPE_DAMAGE_CONDITION_LIST, "If specified, the specified actions/modifiers will only be run/applied if these conditions are fulfilled by the damage dealt by the \"actor\" entity.")
                    .addExampleObject(new ModifyDamageDealtAbility(List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(4.0), KryptoniteModifiers.Operation.ADD_BASE_EARLY)), List.of(), List.of(), List.of(), Optional.empty(), Optional.empty(), Optional.empty(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ModifyDamageDealtAbility(List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(-1.0), KryptoniteModifiers.Operation.MULTIPLY_BASE_ADDITIVE)), List.of(), List.of(), List.of(), Optional.empty(), Optional.of(new OwnerBiCondition()), Optional.empty(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}