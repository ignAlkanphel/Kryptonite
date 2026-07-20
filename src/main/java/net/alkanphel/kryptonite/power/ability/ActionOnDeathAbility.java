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

public class ActionOnDeathAbility extends Ability {

    public static final MapCodec<ActionOnDeathAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions", List.of()).forGetter(a -> a.biEntityActions),
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(a -> a.biEntityConditions),
            DamageCondition.LIST_CODEC.optionalFieldOf("damage_conditions", List.of()).forGetter(a -> a.damageConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnDeathAbility::new));

    public final List<BiAction> biEntityActions;
    public final List<BiCondition> biEntityConditions;
    public final List<DamageCondition> damageConditions;

    public ActionOnDeathAbility(List<BiAction> biEntityActions, List<BiCondition> biEntityConditions, List<DamageCondition> damageConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
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

    public void onDeath(Entity attacker, LivingEntity holder) {
        if (!biEntityActions.isEmpty()) BiAction.runList(biEntityActions, attacker, holder);
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_DEATH.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnDeathAbility> {

        @Override
        public MapCodec<ActionOnDeathAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnDeathAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action On Death")
                    .setDescription("Runs bi actions when the entity dies. In the context of this ability, the \"actor\" is the killer & \"target\" the entity that died.")
                    .addOptional("bientity_actions", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "The bi actions to run on either or both \"actor\" & \"target\" entities.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, only runs the bi actions if these conditions are fulfilled by either or both \"actor\" & \"target\" entities.")
                    .addOptional("damage_conditions", KryptoniteDocumented.TYPE_DAMAGE_CONDITION_LIST, "If specified, only runs the bi actions if the killing damage of the \"actor\" entity matches these damage conditions.")
                    .addExampleObject(new ActionOnDeathAbility(List.of(new TargetActionBiAction(List.of(new RunCommandAction(new ParsedCommands("say Action on death (target_action)!"))))), List.of(), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnDeathAbility(List.of(new ActorActionBiAction(List.of(new RunCommandAction(new ParsedCommands("say Action on death (actor_action)!"))))), List.of(), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}