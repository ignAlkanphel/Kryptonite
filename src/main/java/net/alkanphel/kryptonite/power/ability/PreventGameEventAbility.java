package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class PreventGameEventAbility extends Ability {

    public static final MapCodec<PreventGameEventAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(ab -> ab.entityActions),
            RegistryCodecs.homogeneousList(Registries.GAME_EVENT).listOf().optionalFieldOf("game_events", List.of()).forGetter(ab -> ab.events),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventGameEventAbility::new));

    public final List<Action> entityActions;
    public final List<HolderSet<GameEvent>> events;

    public PreventGameEventAbility(List<Action> entityActions, List<HolderSet<GameEvent>> events, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.events = events;
    }

    public boolean doesPrevent(Holder<GameEvent> event) {
        if (!events.isEmpty()) {
            for (HolderSet<GameEvent> set : events) {
                if (!set.contains(event)) {
                    return false;
                }
            }
        }

        return true;
    }

    public void runActions(LivingEntity entity) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(entity));
    }

    @Override
    public AbilitySerializer<PreventGameEventAbility> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_GAME_EVENT.get();
    }

    public static class Serializer extends AbilitySerializer<PreventGameEventAbility> {

        @Override
        public MapCodec<PreventGameEventAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventGameEventAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Prevents the entity from emitting game events.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "The actions to run on the entity when the game events are prevented.")
                    .addOptional("game_events", KryptoniteDocumented.TYPE_GAME_EVENT_HOLDER_SET, "The game events to prevent.")
                    .addExampleObject(new PreventGameEventAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say A game event was prevented!")))), List.of(HolderSet.direct(GameEvent.HIT_GROUND)), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventGameEventAbility(List.of(), List.of(HolderSet.direct(GameEvent.TELEPORT, GameEvent.STEP)), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}