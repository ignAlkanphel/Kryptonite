package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.AsSourceAction;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class ActionOnDeathAbility extends Ability {

    public static final MapCodec<ActionOnDeathAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            Condition.CODEC.optionalFieldOf("entity_conditions", TrueCondition.INSTANCE).forGetter(a -> a.entityConditions),
            DamageCondition.LIST_CODEC.optionalFieldOf("damage_conditions", List.of()).forGetter(a -> a.damageConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnDeathAbility::new));

    public final List<Action> entityActions;
    public final Condition entityConditions;
    public final List<DamageCondition> damageConditions;

    public ActionOnDeathAbility(List<Action> entityActions, Condition entityConditions, List<DamageCondition> damageConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.entityConditions = entityConditions;
        this.damageConditions = damageConditions;
    }

    public boolean doesApply(Entity source, LivingEntity target, DamageSource damageSource, float amount, AbilityInstance<?> instance) {
        if (!damageConditions.isEmpty() && !DamageCondition.checkConditions(damageConditions, damageSource, amount)) {
            return false;
        }

        return entityConditions.test(DataContext.forAffected(target, source, instance));
    }

    public void onDeath(Entity source, LivingEntity target, AbilityInstance<?> instance) {
        if (!entityActions.isEmpty()) {
            Action.runList(entityActions, DataContext.forAffected(target, source, instance));
        }
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
                    .setDescription("Performs actions when the entity that has this ability dies. For this ability, the \"source\" is the entity responsible for the damage & the \"target\" is the entity that died.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "Action(s) performed on the target.")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) that the target must meet for the actions to perform.")
                    .addOptional("damage_conditions", KryptoniteDocumented.TYPE_DAMAGE_CONDITION_LIST, "Damage condition(s) that the killing damage must meet for the actions to perform.")
                    .addExampleObject(new ActionOnDeathAbility(List.of(new RunCommandAction(new ParsedCommands("say I was killed!"))), TrueCondition.INSTANCE, List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnDeathAbility(List.of(new AsSourceAction(List.of(new RunCommandAction(new ParsedCommands("say I killed something!"))))), TrueCondition.INSTANCE, List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}