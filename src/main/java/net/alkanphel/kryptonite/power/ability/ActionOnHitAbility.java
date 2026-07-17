package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.action.bi.meta.ActorActionBiAction;
import net.alkanphel.kryptonite.power.logic.action.bi.meta.TargetActionBiAction;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.meta.TargetConditionBiCondition;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.HealthCondition;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class ActionOnHitAbility extends Ability {

    public static final MapCodec<ActionOnHitAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions", List.of()).forGetter(a -> a.biEntityActions),
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(a -> a.biEntityConditions),
            DamageCondition.LIST_CODEC.optionalFieldOf("damage_conditions", List.of()).forGetter(a -> a.damageConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnHitAbility::new));

    public final List<BiAction> biEntityActions;
    public final List<BiCondition> biEntityConditions;
    public final List<DamageCondition> damageConditions;

    public ActionOnHitAbility(List<BiAction> biEntityActions, List<BiCondition> biEntityConditions, List<DamageCondition> damageConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.biEntityActions = biEntityActions;
        this.biEntityConditions = biEntityConditions;
        this.damageConditions = damageConditions;
    }

    public boolean doesApply(LivingEntity holder, Entity target, DamageSource source, float amount) {
        if (!damageConditions.isEmpty() && !DamageCondition.checkConditions(damageConditions, source, amount)) {
            return false;
        }

        if (!biEntityConditions.isEmpty() && !BiCondition.checkConditions(biEntityConditions, holder, target)) {
            return false;
        }

        return true;
    }

    public void onHit(LivingEntity holder, Entity target) {
        if (!biEntityActions.isEmpty()) BiAction.runList(biEntityActions, holder, target);
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
                    .setDescription("Runs actions when the entity that has this ability hits another entity. In the context of this ability, the \"actor\" is the ability holder & \"target\" the hit entity.")
                    .addOptional("bientity_actions", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "The bi actions to run on either or both \"actor\" & \"target\" entities.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, the actions will only be run if these bi conditions are fulfilled by either or both \"actor\" & \"target\" entities.")
                    .addOptional("damage_conditions", KryptoniteDocumented.TYPE_DAMAGE_CONDITION_LIST, "If specified, the actions will only run if these damage conditions are fulfilled by the damage dealt by the \"actor\" entity.")
                    .addExampleObject(new ActionOnHitAbility(List.of(new TargetActionBiAction(List.of(new RunCommandAction(new ParsedCommands("say Action on hit (target_action)!"))))), List.of(), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnHitAbility(List.of(new ActorActionBiAction(List.of(new RunCommandAction(new ParsedCommands("say Action on hit (actor_action)!"))))), List.of(), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnHitAbility(List.of(new TargetActionBiAction(List.of(new RunCommandAction(new ParsedCommands("execute as @s at @s run summon minecraft:zombie"))))), List.of(new TargetConditionBiCondition(new HealthCondition(0F, 0F))), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}