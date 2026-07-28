package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class ActionOnGameEventAbility extends Ability {

    public static final MapCodec<ActionOnGameEventAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(ab -> ab.entityActions),
            PalladiumHolderSet.codec(Registries.GAME_EVENT).optionalFieldOf("game_events", PalladiumHolderSet.direct(HolderSet.empty())).forGetter(ab -> ab.gameEvents),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnGameEventAbility::new));

    public final List<Action> entityActions;
    public final PalladiumHolderSet<GameEvent> gameEvents;

    public ActionOnGameEventAbility(List<Action> entityActions, PalladiumHolderSet<GameEvent> gameEvents, AbilityProperties properties, AbilityStateManager stateManager, List<EnergyBarUsage> energyBarUsages) {
        super(properties, stateManager, energyBarUsages);
        this.entityActions = entityActions;
        this.gameEvents = gameEvents;
    }

    public boolean doesApply(LivingEntity entity, Holder<GameEvent> event) {
        return gameEvents.resolve(entity.registryAccess()).contains(event);
    }

    public void runActions(LivingEntity entity) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(entity));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_GAME_EVENT.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnGameEventAbility> {

        @Override
        public MapCodec<ActionOnGameEventAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnGameEventAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action On Game Event")
                    .setDescription("Runs actions upon the entity emitting game events.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "The actions to run on the entity when the game events are emitted.")
                    .addOptional("game_events", KryptoniteDocumented.TYPE_GAME_EVENT_HOLDER_SET, "If specified, the actions will only run when these game events are emitted.")
                    .addExampleObject(new ActionOnGameEventAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Action on game event!")))), PalladiumHolderSet.direct(HolderSet.direct(GameEvent.STEP)), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}