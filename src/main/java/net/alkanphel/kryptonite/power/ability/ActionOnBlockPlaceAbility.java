package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.alkanphel.kryptonite.power.logic.filter.BlockFilter;
import net.alkanphel.kryptonite.power.logic.filter.ItemFilter;
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
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(a -> a.entityActions),
            BlockAction.LIST_CODEC.optionalFieldOf("place_to_actions", List.of()).forGetter(a -> a.placeToActions),
            BlockAction.LIST_CODEC.optionalFieldOf("place_on_actions", List.of()).forGetter(a -> a.placeOnActions),
            BlockFilter.CODEC.optionalFieldOf("place_to_filters", BlockFilter.EMPTY).forGetter(a -> a.placeToFilter),
            BlockFilter.CODEC.optionalFieldOf("place_on_filters", BlockFilter.EMPTY).forGetter(a -> a.placeOnFilter),
            Direction.CODEC.listOf().xmap(EnumSet::copyOf, List::copyOf).optionalFieldOf("directions", EnumSet.allOf(Direction.class)).forGetter(a -> a.directions),
            InteractionPrioritizedFields.CODEC.forGetter(a -> new InteractionPrioritizedFields(new InteractionFields(a.heldItemActions, a.heldItemFilter, a.resultItemActions, a.resultStack, a.hands, a.actionResult), a.getPriority())),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, (entityActions, placeToActions, placeOnActions, placeToFilter, placeOnFilter, directions, activeFields, properties, state, energyBarUsages) -> {
        InteractionFields interaction = activeFields.interaction();
        return new ActionOnBlockPlaceAbility(entityActions, placeToActions, placeOnActions, placeToFilter, placeOnFilter, directions, interaction.heldItemActions(), interaction.heldItemFilter(), interaction.resultItemActions(), interaction.resultStack(), interaction.hands(), interaction.actionResult(), activeFields.priority(), properties, state, energyBarUsages);
    }));

    public final List<Action> entityActions;
    public final List<BlockAction> placeToActions, placeOnActions;
    public final BlockFilter placeToFilter;
    public final BlockFilter placeOnFilter;
    public final EnumSet<Direction> directions;

    public ActionOnBlockPlaceAbility(List<Action> entityActions, List<BlockAction> placeToActions, List<BlockAction> placeOnActions, BlockFilter placeToFilter, BlockFilter placeOnFilter, EnumSet<Direction> directions, List<ItemAction> heldItemActions, ItemFilter heldItemFilter, List<ItemAction> resultItemActions, Optional<ItemStack> resultStack, EnumSet<InteractionHand> hands, InteractionResult actionResult, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(heldItemActions, heldItemFilter, resultItemActions, resultStack, hands, actionResult, priority, properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.placeToActions = placeToActions;
        this.placeOnActions = placeOnActions;
        this.placeToFilter = placeToFilter;
        this.placeOnFilter = placeOnFilter;
        this.directions = directions;
    }

    public boolean shouldRun(DataContext context, ItemStack heldStack, InteractionHand hand, BlockPos toPos, BlockPos onPos, Direction direction) {
        if (!super.shouldRun(hand, heldStack) || !directions.contains(direction)) return false;
        var level = context.getLivingEntity().level();

        if (!placeOnFilter.isEmpty() && !placeOnFilter.test(level, onPos)) {
            return false;
        }

        if (!placeToFilter.isEmpty() && !placeToFilter.test(level, toPos)) {
            return false;
        }

        return true;
    }

    public void runOtherActions(DataContext context, BlockPos toPos, BlockPos onPos, Direction direction) {
        var entity = context.getLivingEntity();
        var level = entity.level();

        Optional<Direction> optDirection = Optional.of(direction);

        if (!placeOnActions.isEmpty()) BlockAction.runList(placeOnActions, level, onPos, optDirection);
        if (!placeToActions.isEmpty()) BlockAction.runList(placeToActions, level, toPos, optDirection);

        if (!entityActions.isEmpty() && level instanceof ServerLevel) {
            Action.runList(entityActions, DataContext.forEntity(entity));
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
                    .addOptional("place_to_filters", BlockFilter.documentation(provider), "Block filter(s) the block at the position of the block the player is about to place must pass for the actions to perform.")
                    .addOptional("place_on_filters", BlockFilter.documentation(provider), "Block filter(s) the block the player is about to place a block on must pass for the actions to perform.")
                    .addOptional("directions", KryptoniteDocumented.TYPE_DIRECTION, "If the actions should be performed if the player is about to place a block at the specified sides of a block.")
                    .addOptional("held_item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "Item action(s) performed on the item the player has used to place a block.")
                    .addOptional("held_item_filters", ItemFilter.documentation(provider), "Item filters(s) the item the player has used to place a block must pass for the item actions to perform.")
                    .addOptional("result_item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "Item action(s) performed on the item given to the player upon placing a block.")
                    .addOptional("result_stack", TYPE_ITEM_STACK, "The item stack given to the player upon placing a block.")
                    .addOptional("hands", KryptoniteDocumented.TYPE_INTERACTION_HAND, "If the actions should be performed if the player used the specified hands when placing a block.")
                    .addOptional("action_result", KryptoniteDocumented.TYPE_INTERACTION_RESULT, "Used to indicate the result of a certain action.")
                    .addOptional("priority", TYPE_INT, "The run priority of this ability. Higher priorities of this ability run first.", 0)
                    .addExampleObject(new ActionOnBlockPlaceAbility(List.of(), List.of(), List.of(), BlockFilter.EMPTY, BlockFilter.EMPTY, EnumSet.allOf(Direction.class), List.of(), ItemFilter.EMPTY, List.of(), Optional.empty(), EnumSet.allOf(InteractionHand.class), InteractionResult.SUCCESS, 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}