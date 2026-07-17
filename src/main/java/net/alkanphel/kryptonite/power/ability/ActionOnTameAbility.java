package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.bi.SetInLoveBiAction;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.meta.TargetConditionBiCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.EntityTypeCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class ActionOnTameAbility extends Ability {

    public static final MapCodec<ActionOnTameAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions", List.of()).forGetter(a -> a.biEntityActions),
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(a -> a.biEntityConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnTameAbility::new));

    public final List<Action> entityActions;
    public final List<BiAction> biEntityActions;
    public final List<BiCondition> biEntityConditions;

    public ActionOnTameAbility(List<Action> entityActions, List<BiAction> biEntityActions, List<BiCondition> biEntityConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.biEntityActions = biEntityActions;
        this.biEntityConditions = biEntityConditions;
    }

    public boolean doesApply(Player tamer, Animal animal) {
        if (!biEntityConditions.isEmpty() && !BiCondition.checkConditions(biEntityConditions, tamer, animal)) {
            return false;
        }

        return true;
    }

    public void runActions(Player tamer, Animal animal) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(tamer));
        if (!biEntityActions.isEmpty()) BiAction.runList(biEntityActions, tamer, animal);
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
                    .setDescription("Runs actions when the player tames an animal. In the context of this ability, the \"actor\" is the player taming the animal & \"target\" the animal being tamed.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "The actions to run on the player that tamed the animal.")
                    .addOptional("bientity_actions", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "The bi actions to run on either or both \"actor\" & \"target\" entities.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, the actions will only be run if these bi conditions are fulfilled by either or both the \"actor\" & \"target\" entities.")
                    .addExampleObject(new ActionOnTameAbility(List.of(new RunCommandAction(new ParsedCommands("say Action on tame!"))), List.of(new SetInLoveBiAction()), List.of(new TargetConditionBiCondition(new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("wolf")))))))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}