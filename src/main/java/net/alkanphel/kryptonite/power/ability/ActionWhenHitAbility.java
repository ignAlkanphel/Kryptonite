package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.action.bi.meta.ActorActionBiAction;
import net.alkanphel.kryptonite.power.logic.action.bi.meta.TargetActionBiAction;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class ActionWhenHitAbility extends Ability {

    public static final MapCodec<ActionWhenHitAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions", List.of()).forGetter(a -> a.biEntityActions),
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(a -> a.biEntityConditions),
            DamageCondition.LIST_CODEC.optionalFieldOf("damage_conditions", List.of()).forGetter(a -> a.damageConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionWhenHitAbility::new));

    public final List<BiAction> biEntityActions;
    public final List<BiCondition> biEntityConditions;
    public final List<DamageCondition> damageConditions;

    public ActionWhenHitAbility(List<BiAction> biEntityActions, List<BiCondition> biEntityConditions, List<DamageCondition> damageConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.biEntityActions = biEntityActions;
        this.biEntityConditions = biEntityConditions;
        this.damageConditions = damageConditions;
    }

    public boolean doesApply(Entity attacker, LivingEntity holder, DamageSource source, float amount) {
        if (!damageConditions.isEmpty() && !DamageCondition.checkConditions(damageConditions, source, amount)) {
            return false;
        }

        if (!biEntityConditions.isEmpty() && !BiCondition.checkConditions(biEntityConditions, attacker, holder)) {
            return false;
        }

        return true;
    }

    public void whenHit(Entity attacker, LivingEntity holder) {
        if (!biEntityActions.isEmpty()) BiAction.runList(biEntityActions, attacker, holder);
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_WHEN_HIT.get();
    }

    public static class Serializer extends AbilitySerializer<ActionWhenHitAbility> {

        @Override
        public MapCodec<ActionWhenHitAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionWhenHitAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action When Hit")
                    .setDescription("Runs actions when the entity that has this ability has been hit by another entity. In the context of this ability, the \"actor\" is the entity that hit & \"target\" is the ability holder.")
                    .addOptional("bientity_actions", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "The bi actions to run on either or both \"actor\" & \"target\" entities.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, the actions will only be run if these bi conditions are fulfilled by either or both \"actor\" & \"target\" entities.")
                    .addOptional("damage_conditions", KryptoniteDocumented.TYPE_DAMAGE_CONDITION_LIST, "If specified, the actions will only run if these damage conditions are fulfilled by the damage dealt by the \"actor\" entity.")
                    .addExampleObject(new ActionWhenHitAbility(List.of(new ActorActionBiAction(List.of(new RunCommandAction(new ParsedCommands("say Action when hit (target_action)!"))))), List.of(), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionWhenHitAbility(List.of(new TargetActionBiAction(List.of(new RunCommandAction(new ParsedCommands("say Action when hit (target_action)!"))))), List.of(), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}