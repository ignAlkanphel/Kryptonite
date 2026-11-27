package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.logic.filter.DamageFilter;
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

public class ActionOnKillAbility extends Ability {

    public static final MapCodec<ActionOnKillAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(a -> a.action),
            Condition.CODEC.optionalFieldOf("target_conditions", TrueCondition.INSTANCE).forGetter(a -> a.targetCondition),
            DamageFilter.CODEC.optionalFieldOf("damage_filters", DamageFilter.EMPTY).forGetter(a -> a.damageFilter),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnKillAbility::new));

    public final List<Action> action;
    public final Condition targetCondition;
    public final DamageFilter damageFilter;

    public ActionOnKillAbility(List<Action> action, Condition targetCondition, DamageFilter damageFilter, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.action = action;
        this.targetCondition = targetCondition;
        this.damageFilter = damageFilter;
    }

    public boolean doesApply(Entity source, LivingEntity target, DamageSource damageSource, float amount, AbilityInstance<?> instance) {
        if (!damageFilter.test(damageSource, amount)) {
            return false;
        }

        return targetCondition.test(DataContext.forAffected(source, target, instance));
    }

    public void runActions(Entity source, LivingEntity target, AbilityInstance<?> instance) {
        if (!action.isEmpty()) Action.runList(action, DataContext.forAffected(target, source, instance));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_KILL.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnKillAbility> {

        @Override
        public MapCodec<ActionOnKillAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnKillAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Performs actions when the entity that has this ability kills another entity. For this ability, the \"source\" is the entity that has this ability, and the \"target\" is the entity that was killed.")
                    .addOptional("actions", TYPE_ACTION_LIST, "Action(s) performed on the target.")
                    .addOptional("target_conditions", TYPE_CONDITION_LIST, "Condition(s) the source must meet for the actions to perform.")
                    .addOptional("damage_filters", DamageFilter.documentation(provider), "Damage filter(s) the killing damage must pass for the actions to perform.")
                    .addExampleObject(new ActionOnKillAbility(List.of(new RunCommandAction(new ParsedCommands("say I killed something!"))), TrueCondition.INSTANCE, DamageFilter.EMPTY, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnKillAbility(List.of(new AsSourceAction(List.of(new RunCommandAction(new ParsedCommands("say I killed something!"))))), TrueCondition.INSTANCE, DamageFilter.EMPTY, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}