package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.damage.DamageTypeDamageCondition;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;
import java.util.Optional;

public class ActionWhenDamageTakenAbility extends Ability {

    public static final MapCodec<ActionWhenDamageTakenAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.fieldOf("entity_actions").forGetter(a -> a.entityActions),
            DamageCondition.CODEC.optionalFieldOf("damage_conditions").forGetter(a -> a.damageConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionWhenDamageTakenAbility::new));

    public final List<Action> entityActions;
    public final Optional<DamageCondition> damageConditions;

    public ActionWhenDamageTakenAbility(List<Action> entityActions, Optional<DamageCondition> damageConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.damageConditions = damageConditions;
    }

    public boolean doesApply(DamageSource source, float amount) {
        return damageConditions.map(condition -> condition.test(source, amount)).orElse(true);
    }

    public void whenHit(LivingEntity holder) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(holder));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_WHEN_DAMAGE_TAKEN.get();
    }

    public static class Serializer extends AbilitySerializer<ActionWhenDamageTakenAbility> {

        @Override
        public MapCodec<ActionWhenDamageTakenAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionWhenDamageTakenAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action When Damage Taken")
                    .setDescription("Runs actions on the entity that has this ability if damage was taken.")
                    .add("entity_actions", TYPE_ACTION_LIST, "The actions to run upon taking damage.")
                    .addOptional("damage_conditions", KryptoniteDocumented.TYPE_DAMAGE_CONDITION_LIST, "If specified, only runs the actions if the damage matches these damage conditions.")
                    .addExampleObject(new ActionWhenDamageTakenAbility(List.of(new RunCommandAction(new ParsedCommands("I'm feeling spicy!"))), Optional.of(new DamageTypeDamageCondition(provider.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeTags.IS_FIRE))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}