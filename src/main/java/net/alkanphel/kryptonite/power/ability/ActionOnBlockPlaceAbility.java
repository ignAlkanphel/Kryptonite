package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.util.apoli.ability.InteractionPrioritizedAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

public class ActionOnBlockPlaceAbility extends InteractionPrioritizedAbility {

    public static final MapCodec<ActionOnBlockPlaceAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(a -> a.action),
            BlockAction.LIST_CODEC.optionalFieldOf("place_to_actions", List.of()).forGetter(a -> a.placeToAction),
            BlockAction.LIST_CODEC.optionalFieldOf("place_on_actions", List.of()).forGetter(a -> a.placeOnAction),
            BlockCondition.LIST_CODEC.optionalFieldOf("place_to_conditions", List.of()).forGetter(a -> a.placeToCondition),
            BlockCondition.LIST_CODEC.optionalFieldOf("place_on_conditions", List.of()).forGetter(a -> a.placeOnCondition),
            Direction.CODEC.listOf().xmap(EnumSet::copyOf, List::copyOf).optionalFieldOf("directions", EnumSet.allOf(Direction.class)).forGetter(a -> a.directions),
            InteractionPrioritizedFields.CODEC.forGetter(a -> new InteractionPrioritizedFields(new InteractionFields(a.heldItemCondition, a.heldItemAction, a.resultItemAction, a.resultStack, a.hands, a.actionResult), a.getPriority())),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, (action, placeToAction, placeOnAction, placeToCondition, placeOnCondition, directions, activeFields, properties, state, energyBarUsages) -> {
        InteractionFields interaction = activeFields.interaction();
        return new ActionOnBlockPlaceAbility(action, placeToAction, placeOnAction, placeToCondition, placeOnCondition, directions, interaction.heldItemAction(), interaction.heldItemCondition(), interaction.resultItemAction(), interaction.resultStack(), interaction.hands(), interaction.actionResult(), activeFields.priority(), properties, state, energyBarUsages);
    }));

    public final List<Action> action;
    public final List<BlockAction> placeToAction, placeOnAction;
    public final List<BlockCondition> placeToCondition;
    public final List<BlockCondition> placeOnCondition;
    public final EnumSet<Direction> directions;

    public ActionOnBlockPlaceAbility(List<Action> action, List<BlockAction> placeToAction, List<BlockAction> placeOnAction, List<BlockCondition> placeToCondition, List<BlockCondition> placeOnCondition, EnumSet<Direction> directions, List<ItemAction> heldItemAction, List<ItemCondition> heldItemCondition, List<ItemAction> resultItemAction, Optional<ItemStack> resultStack, EnumSet<InteractionHand> hands, InteractionResult actionResult, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(heldItemCondition, heldItemAction, resultItemAction, resultStack, hands, actionResult, priority, properties, conditions, energyBarUsages);
        this.action = action;
        this.placeToAction = placeToAction;
        this.placeOnAction = placeOnAction;
        this.placeToCondition = placeToCondition;
        this.placeOnCondition = placeOnCondition;
        this.directions = directions;
    }

    public boolean shouldRun(DataContext context, ItemStack heldStack, InteractionHand hand, BlockPos toPos, BlockPos onPos, Direction direction) {
        if (!super.shouldRun(hand, heldStack) || !directions.contains(direction)) return false;
        var level = context.getLivingEntity().level();

        if (!placeOnCondition.isEmpty() && !BlockCondition.checkConditions(placeOnCondition, level, onPos)) {
            return false;
        }

        if (!placeToCondition.isEmpty() && !BlockCondition.checkConditions(placeToCondition, level, toPos)) {
            return false;
        }

        return true;
    }

    public void runOtherActions(DataContext context, BlockPos toPos, BlockPos onPos, Direction direction) {
        var entity = context.getLivingEntity();
        var level = entity.level();

        Optional<Direction> optDirection = Optional.of(direction);

        if (!placeOnAction.isEmpty()) BlockAction.runList(placeOnAction, level, onPos, optDirection);
        if (!placeToAction.isEmpty()) BlockAction.runList(placeToAction, level, toPos, optDirection);

        if (!action.isEmpty() && level instanceof ServerLevel) {
            Action.runList(action, DataContext.forEntity(entity));
        }
    }

    public void runItemActions(DataContext context, InteractionHand hand) {
        if (context.getLivingEntity() instanceof Player player) {
            this.performSourceItemStuff(player, hand);
        }
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_BLOCK_PLACE.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnBlockPlaceAbility> {

        @Override
        public MapCodec<ActionOnBlockPlaceAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnBlockPlaceAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Performs actions upon placing a block.")
                    .addOptional("actions", TYPE_ACTION_LIST, "Action(s) performed on the player upon them placing a block.")
                    .addOptional("place_to_actions", KryptoniteDocumented.TYPE_BLOCK_ACTION_LIST, "Block action(s) performed at the position of the block the player has placed.")
                    .addOptional("place_on_actions", KryptoniteDocumented.TYPE_BLOCK_ACTION_LIST, "Block action(s) performed on the block the player placed a block on.")
                    .addOptional("place_to_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "Block condition(s) the block at the position of the block the player is about to place must meet for the actions to perform.")
                    .addOptional("place_on_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "Block condition(s) the block the player is about to place a block on must meet for the actions to perform.")
                    .addOptional("directions", KryptoniteDocumented.TYPE_DIRECTION, "If the actions should be performed if the player is about to place a block at the specified sides of a block.")
                    .addOptional("held_item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "Item action(s) performed on the item the player has used to place a block.")
                    .addOptional("held_item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "Item condition(s) the item the player has used to place a block must meet for the item actions to perform.")
                    .addOptional("result_item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "Item action(s) performed on the item given to the player upon placing a block.")
                    .addOptional("result_stack", TYPE_ITEM_STACK, "The item stack given to the player upon placing a block.")
                    .addOptional("hands", KryptoniteDocumented.TYPE_INTERACTION_HAND, "If the actions should be performed if the player used the specified hands when placing a block.")
                    .addOptional("action_result", KryptoniteDocumented.TYPE_INTERACTION_RESULT, "Used to indicate the result of a certain action.")
                    .addOptional("priority", TYPE_INT, "The run priority of this ability. Higher priorities of this ability run first.", 0)
                    .addExampleObject(new ActionOnBlockPlaceAbility(List.of(), List.of(), List.of(), List.of(), List.of(), EnumSet.allOf(Direction.class), List.of(), List.of(), List.of(), Optional.empty(), EnumSet.allOf(InteractionHand.class), InteractionResult.SUCCESS, 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}