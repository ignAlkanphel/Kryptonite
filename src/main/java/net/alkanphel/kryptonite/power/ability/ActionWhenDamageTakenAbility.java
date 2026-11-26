package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.logic.filter.DamageFilter;
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
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class ActionWhenDamageTakenAbility extends Ability {

    public static final MapCodec<ActionWhenDamageTakenAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.fieldOf("actions").forGetter(a -> a.entityActions),
            DamageFilter.CODEC.optionalFieldOf("damage_filters", DamageFilter.EMPTY).forGetter(a -> a.damageFilter),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionWhenDamageTakenAbility::new));

    public final List<Action> entityActions;
    public final DamageFilter damageFilter;

    public ActionWhenDamageTakenAbility(List<Action> entityActions, DamageFilter damageFilter, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.damageFilter = damageFilter;
    }

    public boolean doesApply(DamageSource source, float amount) {
        return damageFilter.test(source, amount);
    }

    public void runActions(LivingEntity holder) {
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
            builder.setDescription("Performs actions upon taking damage.")
                    .add("actions", TYPE_ACTION_LIST, "Action(s) performed upon taking damage.")
                    .addOptional("damage_filters", DamageFilter.documentation(provider), "Damage filter(s) the damage source must pass for the actions to perform.")
                    .addExampleObject(new ActionWhenDamageTakenAbility(List.of(new RunCommandAction(new ParsedCommands("say I'm feeling spicy!"))), DamageFilter.ofDamageTypes(PalladiumHolderSet.direct(provider.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeTags.IS_FIRE))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}