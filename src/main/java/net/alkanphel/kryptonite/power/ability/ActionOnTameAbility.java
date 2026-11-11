package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.AsSourceAction;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.EntityTypeCondition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class ActionOnTameAbility extends Ability {

    public static final MapCodec<ActionOnTameAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            Condition.CODEC.optionalFieldOf("entity_conditions", TrueCondition.INSTANCE).forGetter(a -> a.entityConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnTameAbility::new));

    public final List<Action> entityActions;
    public final Condition entityConditions;

    public ActionOnTameAbility(List<Action> entityActions, Condition entityConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.entityConditions = entityConditions;
    }

    public boolean doesApply(Player source, Animal target, AbilityInstance<?> abilityInstance) {
        return entityConditions.test(DataContext.forAffected(target, source, abilityInstance));
    }

    public void runActions(Player source, Animal target, AbilityInstance<?> abilityInstance) {
        Action.runList(entityActions, DataContext.forAffected(target, source, abilityInstance));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_TAME.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnTameAbility> {

        @Override
        public MapCodec<ActionOnTameAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnTameAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action On Tame")
                    .setDescription("Performs actions when a player tames an animal. For this ability, the \"source\" is the player taming the animal & the \"target\" is the animal being tamed.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "Action(s) performed on the animal being tamed.")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) the animal must meet for the actions to perform.")
                    .addExampleObject(new ActionOnTameAbility(List.of(new RunCommandAction(new ParsedCommands("say You tamed me!"))), TrueCondition.INSTANCE, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnTameAbility(List.of(new RunCommandAction(new ParsedCommands("say You tamed me!")), new AsSourceAction(List.of(new RunCommandAction(new ParsedCommands("say You tamed them"))))), new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("wolf")))))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}