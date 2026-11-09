package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
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

public class ActionOnCriticalHitAbility extends Ability {

    public static final MapCodec<ActionOnCriticalHitAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            Condition.CODEC.optionalFieldOf("entity_conditions", TrueCondition.INSTANCE).forGetter(a -> a.entityConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnCriticalHitAbility::new));

    public final List<Action> entityActions;
    public final Condition entityConditions;

    public ActionOnCriticalHitAbility(List<Action> entityActions, Condition entityConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.entityConditions = entityConditions;
    }

    public boolean doesApply(Player source, Entity target, AbilityInstance<?> abilityInstance) {
        return entityConditions.test(DataContext.forAffected(target, source, abilityInstance));
    }

    public void runActions(Player source, Entity target, AbilityInstance<?> abilityInstance) {
        Action.runList(entityActions, DataContext.forAffected(target, source, abilityInstance));
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
                    .setDescription("Performs actions when the player with this ability lands a critical hit. For this ability, the \"source\" is the attacking player & the \"target\" is the entity being hit.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "Action(s) performed on the target when a critical hit is dealt.")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) that the target must meet to perform the actions.")
                    .addExampleObject(new ActionOnCriticalHitAbility(List.of(new RunCommandAction(new ParsedCommands("say Action on critical hit!"))), TrueCondition.INSTANCE, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}