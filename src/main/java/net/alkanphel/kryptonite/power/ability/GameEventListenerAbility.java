package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.action.bi.meta.TargetActionBiAction;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.meta.ActorConditionBiCondition;
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
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.EntityTypeCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class GameEventListenerAbility extends Ability {

    public static final MapCodec<GameEventListenerAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions", List.of()).forGetter(a -> a.biEntityActions),
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(a -> a.biEntityConditions),
            BlockAction.LIST_CODEC.optionalFieldOf("block_actions", List.of()).forGetter(a -> a.blockActions),
            BlockCondition.LIST_CODEC.optionalFieldOf("block_conditions", List.of()).forGetter(a -> a.blockConditions),
            PalladiumHolderSet.codec(Registries.GAME_EVENT).optionalFieldOf("game_events", PalladiumHolderSet.direct(HolderSet.empty())).forGetter(ab -> ab.gameEvents),
            Codec.STRING.xmap(value -> GameEventListener.DeliveryMode.valueOf(value.toUpperCase(Locale.ROOT)), value -> value.name().toLowerCase(Locale.ROOT)).optionalFieldOf("delivery_mode", GameEventListener.DeliveryMode.BY_DISTANCE).forGetter(a -> a.deliveryMode),
            Value.CODEC.optionalFieldOf("range", new StaticValue(16)).forGetter(a -> a.range),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, GameEventListenerAbility::new));

    public final List<BiAction> biEntityActions;
    public final List<BiCondition> biEntityConditions;
    public final List<BlockAction> blockActions;
    public final List<BlockCondition> blockConditions;
    public final PalladiumHolderSet<GameEvent> gameEvents;
    public final GameEventListener.DeliveryMode deliveryMode;
    public final Value range;

    public GameEventListenerAbility(List<BiAction> biEntityActions, List<BiCondition> biEntityConditions, List<BlockAction> blockActions, List<BlockCondition> blockConditions, PalladiumHolderSet<GameEvent> gameEvents, GameEventListener.DeliveryMode deliveryMode, Value range, AbilityProperties properties, AbilityStateManager stateManager, List<EnergyBarUsage> energyBarUsages) {
        super(properties, stateManager, energyBarUsages);
        this.biEntityActions = biEntityActions;
        this.biEntityConditions = biEntityConditions;
        this.blockActions = blockActions;
        this.blockConditions = blockConditions;
        this.gameEvents = gameEvents;
        this.deliveryMode = deliveryMode;
        this.range = range;
    }

    public boolean doesListen(LivingEntity living, AbilityInstance<?> instance, VanillaGameEvent e) {
        if (!gameEvents.resolve(living.registryAccess()).contains(e.getVanillaEvent())) {
            return false;
        }

        if (deliveryMode == GameEventListener.DeliveryMode.UNSPECIFIED) {
            return true;
        }

        int range = this.range.getAsInt(DataContext.forAbility(living, instance));

        return living.position().distanceToSqr(e.getEventPosition()) <= (double) range * range;
    }

    public void run(LivingEntity living, VanillaGameEvent e) {
        Entity source = e.getCause();
        BlockPos blockPos = BlockPos.containing(living.getX(), living.getBoundingBox().minY - 0.01, living.getZ());

        if (source != null && !BiCondition.checkConditions(biEntityConditions, source, living)) {
            return;
        }

        if (!BlockCondition.checkConditions(blockConditions, living.level(), blockPos)) {
            return;
        }

        if (source != null && !biEntityActions.isEmpty()) {
            BiAction.runList(biEntityActions, source, living);
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
                    .setDescription("Runs actions upon entities emitting game events. In the context of this ability, the \"actor\" is the entity that emitted the game events & the \"target\" is the entity that has this ability.")
                    .addOptional("bientity_actions", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "If specified, these actions will be run on either or both the \"actor\" & \"target\" entities.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, only applies when these conditions are fulfilled by either or both the \"actor\" & \"target\" entities.")
                    .addOptional("block_actions", KryptoniteDocumented.TYPE_BLOCK_ACTION_LIST, "If specified, these actions will be run on the block being stood on by the entity detecting the game events.")
                    .addOptional("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, the block actions will only run when these conditions are fulfilled by the block being stood on by the entity detecting the game events.")
                    .addOptional("game_events", KryptoniteDocumented.TYPE_GAME_EVENT_HOLDER_SET, "If specified, the specified actions will only run when these game events are triggered.")
                    .addOptional("delivery_mode", SettingType.enumList(GameEventListener.DeliveryMode.values()), "Whether game events trigger the actions to run regardless of distance or only when they occur within the specified range.", GameEventListener.DeliveryMode.BY_DISTANCE)
                    .addOptional("range", TYPE_VALUE, "The range that game events will be detected within.", 16)
                    .addExampleObject(new GameEventListenerAbility(List.of(new TargetActionBiAction(List.of(new RunCommandAction(new ParsedCommands("say I hear this annoying villager within 5 netherblocking blocks away while I'm standing on sand!"))))), List.of(new ActorConditionBiCondition(new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("villager")))))))), List.of(), List.of(new BlockBlockCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("sand"))))))), PalladiumHolderSet.direct(HolderSet.direct(GameEvent.HIT_GROUND, GameEvent.STEP)), GameEventListener.DeliveryMode.BY_DISTANCE, new StaticValue(5), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}