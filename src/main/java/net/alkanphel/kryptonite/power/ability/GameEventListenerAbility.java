package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.condition.block.BlockBlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.AsSourceCondition;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.EntityTypeCondition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class GameEventListenerAbility extends Ability {

    public static final MapCodec<GameEventListenerAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            Condition.CODEC.optionalFieldOf("entity_conditions", TrueCondition.INSTANCE).forGetter(a -> a.entityConditions),
            BlockAction.LIST_CODEC.optionalFieldOf("block_actions", List.of()).forGetter(a -> a.blockActions),
            BlockCondition.LIST_CODEC.optionalFieldOf("block_conditions", List.of()).forGetter(a -> a.blockConditions),
            PalladiumHolderSet.codec(Registries.GAME_EVENT).optionalFieldOf("game_events", PalladiumHolderSet.direct(HolderSet.empty())).forGetter(ab -> ab.gameEvents),
            Codec.STRING.xmap(value -> GameEventListener.DeliveryMode.valueOf(value.toUpperCase(Locale.ROOT)), value -> value.name().toLowerCase(Locale.ROOT)).optionalFieldOf("delivery_mode", GameEventListener.DeliveryMode.BY_DISTANCE).forGetter(a -> a.deliveryMode),
            Value.CODEC.optionalFieldOf("range", new StaticValue(16)).forGetter(a -> a.range),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, GameEventListenerAbility::new));

    public final List<Action> entityActions;
    public final Condition entityConditions;
    public final List<BlockAction> blockActions;
    public final List<BlockCondition> blockConditions;
    public final PalladiumHolderSet<GameEvent> gameEvents;
    public final GameEventListener.DeliveryMode deliveryMode;
    public final Value range;

    public GameEventListenerAbility(List<Action> entityActions, Condition entityConditions, List<BlockAction> blockActions, List<BlockCondition> blockConditions, PalladiumHolderSet<GameEvent> gameEvents, GameEventListener.DeliveryMode deliveryMode, Value range, AbilityProperties properties, AbilityStateManager stateManager, List<EnergyBarUsage> energyBarUsages) {
        super(properties, stateManager, energyBarUsages);
        this.entityActions = entityActions;
        this.entityConditions = entityConditions;
        this.blockActions = blockActions;
        this.blockConditions = blockConditions;
        this.gameEvents = gameEvents;
        this.deliveryMode = deliveryMode;
        this.range = range;
    }

    public boolean doesApply(LivingEntity living, AbilityInstance<?> instance, VanillaGameEvent e) {
        if (!gameEvents.resolve(living.registryAccess()).contains(e.getVanillaEvent())) {
            return false;
        }

        if (deliveryMode == GameEventListener.DeliveryMode.UNSPECIFIED) {
            return true;
        }

        int range = this.range.getAsInt(DataContext.forAbility(living, instance));

        return living.position().distanceToSqr(e.getEventPosition()) <= (double) range * range;
    }

    public void apply(LivingEntity living, VanillaGameEvent e, AbilityInstance<?> instance) {
        Entity source = e.getCause();
        BlockPos blockPos = BlockPos.containing(living.getX(), living.getBoundingBox().minY - 0.01, living.getZ());

        if (source != null && !entityConditions.test(DataContext.forAffected(living, source, instance))) {
            return;
        }

        if (!BlockCondition.checkConditions(blockConditions, living.level(), blockPos)) {
            return;
        }

        if (source != null && !entityActions.isEmpty()) {
            Action.runList(entityActions, DataContext.forAffected(living, source, instance));
        }

        if (!blockActions.isEmpty()) {
            BlockAction.runList(blockActions, living.level(), blockPos, Optional.empty());
        }
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.GAME_EVENT_LISTENER.get();
    }

    public static class Serializer extends AbilitySerializer<GameEventListenerAbility> {

        @Override
        public MapCodec<GameEventListenerAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, GameEventListenerAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Game Event Listener")
                    .setDescription("Performs actions when entities emit game events. For this ability, the \"source\" is the entity that emitted the game event & the \"target\" is the entity listening for it.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "Action(s) performed on the entity listening for the game event.")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) that the target must meet to perform the actions.")
                    .addOptional("block_actions", KryptoniteDocumented.TYPE_BLOCK_ACTION_LIST, "Action(s) performed on the block being stood on by the entity detecting the game events.")
                    .addOptional("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "Condition(s) that the block being stood on must meet to perform the block actions.")
                    .addOptional("game_events", KryptoniteDocumented.TYPE_GAME_EVENT_HOLDER_SET, "If specified, the actions will only perform when these game events are triggered.")
                    .addOptional("delivery_mode", SettingType.enumList(GameEventListener.DeliveryMode.values()), "Whether game events trigger the actions to perform regardless of distance or only when they occur within the specified range.", GameEventListener.DeliveryMode.BY_DISTANCE)
                    .addOptional("range", TYPE_INT_VALUE, "Range game events will be detected within.", 16)
                    .addExampleObject(new GameEventListenerAbility(List.of(new RunCommandAction(new ParsedCommands("say I hear this annoying villager within 5 netherblocking blocks away while I'm standing on sand!"))), new AsSourceCondition(Collections.singletonList(new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("villager")))))))), List.of(), List.of(new BlockBlockCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("sand"))))))), PalladiumHolderSet.direct(HolderSet.direct(GameEvent.HIT_GROUND, GameEvent.STEP)), GameEventListener.DeliveryMode.BY_DISTANCE, new StaticValue(5), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}