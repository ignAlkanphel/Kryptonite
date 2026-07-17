package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.util.apoli.BlockUsagePhase;
import net.alkanphel.kryptonite.util.apoli.ability.InteractionPrioritizedAbility;
import net.alkanphel.kryptonite.util.apoli.ability.Prioritized;
import net.alkanphel.kryptonite.util.apoli.ability.PriorityPhase;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class ActionOnBlockUseAbility extends InteractionPrioritizedAbility {

    public static final MapCodec<ActionOnBlockUseAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            BlockAction.LIST_CODEC.optionalFieldOf("block_actions", List.of()).forGetter(a -> a.blockActions),
            BlockCondition.LIST_CODEC.optionalFieldOf("block_conditions", List.of()).forGetter(a -> a.blockConditions),
            BlockUsagePhase.CODEC.listOf().xmap(EnumSet::copyOf, List::copyOf).optionalFieldOf("use_phases", EnumSet.allOf(BlockUsagePhase.class)).forGetter(a -> a.usePhases),
            Direction.CODEC.listOf().xmap(EnumSet::copyOf, List::copyOf).optionalFieldOf("directions", EnumSet.allOf(Direction.class)).forGetter(a -> a.directions),
            InteractionPrioritizedFields.CODEC.forGetter(a -> new InteractionPrioritizedFields(new InteractionFields(a.heldItemActions, a.heldItemConditions, a.resultItemActions, a.resultStack, a.hands, a.actionResult), a.getPriority())),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, (entityActions, blockActions, blockConditions, usePhases, directions, activeFields, properties, state, energyBarUsages) -> {
        InteractionFields interaction = activeFields.interaction();
        return new ActionOnBlockUseAbility(entityActions, blockActions, blockConditions, usePhases, directions, interaction.heldItemActions(), interaction.heldItemConditions(), interaction.resultItemActions(), interaction.resultStack(), interaction.hands(), interaction.actionResult(), activeFields.priority(), properties, state, energyBarUsages);
    }));

    public final List<Action> entityActions;
    public final List<BlockAction> blockActions;
    public final List<BlockCondition> blockConditions;
    public final EnumSet<BlockUsagePhase> usePhases;
    public final EnumSet<Direction> directions;

    public ActionOnBlockUseAbility(List<Action> entityActions, List<BlockAction> blockActions, List<BlockCondition> blockConditions, EnumSet<BlockUsagePhase> usePhases, EnumSet<Direction> directions, List<ItemAction> heldItemActions, List<ItemCondition> heldItemConditions, List<ItemAction> resultItemActions, Optional<ItemStack> resultStack, EnumSet<InteractionHand> hands, InteractionResult actionResult, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(heldItemActions, heldItemConditions, resultItemActions, resultStack, hands, actionResult, priority, properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.blockActions = blockActions;
        this.blockConditions = blockConditions;
        this.usePhases = usePhases;
        this.directions = directions;
    }

    public boolean shouldRun(DataContext context, BlockUsagePhase usePhase, PriorityPhase priorityPhase, InteractionHand hand, ItemStack heldStack, BlockHitResult hitResult) {
        if (!priorityPhase.test(this.getPriority()) || !usePhases.contains(usePhase) || !directions.contains(hitResult.getDirection()) || !super.shouldRun(hand, heldStack)) {
            return false;
        }

        if (!blockConditions.isEmpty() && !BlockCondition.checkConditions(blockConditions, context.getLivingEntity().level(), hitResult.getBlockPos())) {
            return false;
        }

        return true;
    }

    public InteractionResult runActions(DataContext context, BlockHitResult hitResult, InteractionHand hand) {
        var entity = context.getLivingEntity();
        var level = entity.level();

        if (!blockActions.isEmpty()) BlockAction.runList(blockActions, level, hitResult.getBlockPos(), Optional.of(hitResult.getDirection()));

        if (!entityActions.isEmpty() && level instanceof ServerLevel) {
            Action.runList(entityActions, DataContext.forEntity(entity));
        }

        if (entity instanceof Player player) {
            performActorItemStuff(player, hand);
        }

        return getActionResult();
    }

    public static InteractionResult interactionResultHelper(Prioritized.CallInstance<ActionOnBlockUseAbility> instances, DataContext context, BlockHitResult hitResult, InteractionHand hand, Player player) {
        InteractionResult[] trackingResult = { InteractionResult.PASS };

        instances.forEachBucketUntil(abilities -> {
            InteractionResult bucketResult = InteractionResult.PASS;

            for (ActionOnBlockUseAbility ability : abilities) {
                InteractionResult currentResult = ability.runActions(context, hitResult, hand);
                if (currentResult != InteractionResult.PASS) {
                    bucketResult = currentResult;
                }
            }

            if (bucketResult == InteractionResult.PASS) return false;

            trackingResult[0] = bucketResult;
            if (bucketResult.consumesAction()) player.swing(hand);
            return true;
        });

        return trackingResult[0];
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_BLOCK_USE.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnBlockUseAbility> {

        @Override
        public MapCodec<ActionOnBlockUseAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnBlockUseAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action On Block Use")
                    .setDescription("Runs actions when the player that has this ability 'uses' (right-clicks) a block.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "If specified, these actions will be run on the player upon using a block.")
                    .addOptional("block_actions", KryptoniteDocumented.TYPE_BLOCK_ACTION_LIST, "If specified, these actions will be run on the block the player used.")
                    .addOptional("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, only blocks that fulfill these conditions fulfilled will be affected.")
                    .addOptional("use_phases", KryptoniteDocumented.TYPE_BLOCK_USAGE_PHASE, "At which point the actions should be run/prevented.")
                    .addOptional("directions", KryptoniteDocumented.TYPE_DIRECTION, "If the specified actions should be run if the player tried to use a block at the specified sides of a block.")
                    .addOptional("held_item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "If specified, the actions will be run on the item the player has used to use a block.")
                    .addOptional("held_item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "If specified, the specified actions will only be run if the item the player has used to use a block fulfills these conditions.")
                    .addOptional("result_item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "If specified, these actions will be run on the item that will be given to the player upon using a block.")
                    .addOptional("result_stack", TYPE_ITEM_STACK, "If specified, this item stack will be given to the player upon using a block.")
                    .addOptional("hands", KryptoniteDocumented.TYPE_INTERACTION_HAND, "If the specified actions should be run if the player used the specified hands when using a block.")
                    .addOptional("action_result", KryptoniteDocumented.TYPE_INTERACTION_RESULT, "Used to indicate the result of a certain action.")
                    .addOptional("priority", TYPE_INT, "The run priority of this ability. Higher priorities of this ability run first.", 0)
                    .addExampleObject(new ActionOnBlockUseAbility(List.of(), List.of(), List.of(), EnumSet.allOf(BlockUsagePhase.class), EnumSet.allOf(Direction.class), List.of(), List.of(), List.of(), Optional.empty(), EnumSet.allOf(InteractionHand.class), InteractionResult.SUCCESS, 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}