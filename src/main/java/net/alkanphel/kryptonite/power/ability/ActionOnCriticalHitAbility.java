package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class ActionOnCriticalHitAbility extends Ability {

    public static final MapCodec<ActionOnCriticalHitAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions", List.of()).forGetter(a -> a.biEntityActions),
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(a -> a.biEntityConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnCriticalHitAbility::new));

    public final List<Action> entityActions;
    public final List<BiAction> biEntityActions;
    public final List<BiCondition> biEntityConditions;

    public ActionOnCriticalHitAbility(List<Action> entityActions, List<BiAction> biEntityActions, List<BiCondition> biEntityConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.biEntityActions = biEntityActions;
        this.biEntityConditions = biEntityConditions;
    }

    public boolean doesApply(Player player, Entity target) {
        return biEntityConditions.isEmpty() || BiCondition.checkConditions(biEntityConditions, player, target);
    }

    public void runActions(Player player, Entity target) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(player));
        if (!biEntityActions.isEmpty()) BiAction.runList(biEntityActions, player, target);
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_CRITICAL_HIT.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnCriticalHitAbility> {

        @Override
        public MapCodec<ActionOnCriticalHitAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnCriticalHitAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action On Critical Hit")
                    .setDescription("Runs actions when the entity that has this ability lands a critical hit. In the context of this ability, the \"actor\" is the attacker entity & \"target\" the hit entity.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "The actions to run upon a critical hit being dealt.")
                    .addOptional("bientity_actions", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "The bi actions to run upon a critical hit being dealt.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, only runs the actions if these conditions are fulfilled..")
                    .addExampleObject(new ActionOnCriticalHitAbility(List.of(new RunCommandAction(new ParsedCommands("Action on critical hit!"))), List.of(), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}