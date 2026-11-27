package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.logic.filter.DamageFilter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
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
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ActionOnDamageAbility extends Ability {

    public static final MapCodec<ActionOnDamageAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.fieldOf("actions").forGetter(a -> a.action),
            Condition.CODEC.optionalFieldOf("target_conditions", TrueCondition.INSTANCE).forGetter(a -> a.targetCondition),
            DamageFilter.CODEC.optionalFieldOf("damage_filters", DamageFilter.EMPTY).forGetter(a -> a.damageFilter),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnDamageAbility::new));

    public final List<Action> action;
    public final Condition targetCondition;
    public final DamageFilter damageFilter;

    public ActionOnDamageAbility(List<Action> action, Condition targetCondition, DamageFilter damageFilter, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.action = action;
        this.targetCondition = targetCondition;
        this.damageFilter = damageFilter;
    }

    public boolean doesApply(LivingEntity holder, @Nullable Entity attacker, DamageSource damageSource, float amount, AbilityInstance<?> instance) {
        if (!damageFilter.test(damageSource, amount)) {
            return false;
        }

        return targetCondition.test(DataContext.forAffected(holder, attacker, instance));
    }

    public void runActions(LivingEntity holder, @Nullable Entity attacker, AbilityInstance<?> instance) {
        if (!action.isEmpty()) Action.runList(action, DataContext.forAffected(holder, attacker, instance));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_DAMAGE.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnDamageAbility> {

        @Override
        public MapCodec<ActionOnDamageAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnDamageAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Performs actions upon taking damage. For this ability, the \"target\" is the entity that has this ability & the \"source\" is the entity that dealt the damage, if there is one.")
                    .add("actions", TYPE_ACTION_LIST, "Action(s) performed upon taking damage.")
                    .addOptional("target_conditions", TYPE_CONDITION_LIST, "Condition(s) the entity that has this ability must meet for the actions to perform.")
                    .addOptional("damage_filters", DamageFilter.documentation(provider), "Damage filter(s) the damage source must pass for the actions to perform.")
                    .addExampleObject(new ActionOnDamageAbility(List.of(new RunCommandAction(new ParsedCommands("say I'm feeling spicy!"))), TrueCondition.INSTANCE, DamageFilter.ofDamageTypes(PalladiumHolderSet.direct(provider.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeTags.IS_FIRE))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnDamageAbility(List.of(new AsSourceAction(List.of(new RunCommandAction(new ParsedCommands("say I hit someone!"))))), TrueCondition.INSTANCE, DamageFilter.EMPTY, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}