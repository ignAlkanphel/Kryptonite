package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.filter.BlockFilter;
import net.alkanphel.kryptonite.power.logic.filter.ItemFilter;
import net.alkanphel.kryptonite.power.logic.operation.BlockOperation;
import net.alkanphel.kryptonite.power.logic.operation.ItemOperation;
import net.alkanphel.kryptonite.util.apoli.ability.InteractionPrioritizedAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class PreventBlockPlaceAbility extends InteractionPrioritizedAbility {

    public static final MapCodec<PreventBlockPlaceAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(a -> a.entityActions),
            BlockOperation.CODEC.optionalFieldOf("place_to_operations", BlockOperation.EMPTY).forGetter(a -> a.placeToOperation),
            BlockOperation.CODEC.optionalFieldOf("place_on_operations", BlockOperation.EMPTY).forGetter(a -> a.placeOnOperation),
            BlockFilter.CODEC.optionalFieldOf("place_to_conditions", BlockFilter.EMPTY).forGetter(a -> a.placeToFilter),
            BlockFilter.CODEC.optionalFieldOf("place_on_conditions", BlockFilter.EMPTY).forGetter(a -> a.placeOnFilter),
            Direction.CODEC.listOf().xmap(EnumSet::copyOf, List::copyOf).optionalFieldOf("directions", EnumSet.allOf(Direction.class)).forGetter(a -> a.directions),
            InteractionPrioritizedFields.CODEC.forGetter(a -> new InteractionPrioritizedFields(new InteractionFields(a.heldItemFilter, a.heldItemOperation, a.resultItemOperation, a.resultStack, a.hands, a.actionResult), a.getPriority())),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, (entityActions, placeToOperation, placeOnOperation, placeToFilter, placeOnFilter, directions, activeFields, properties, state, energyBarUsages) -> {
        InteractionFields interaction = activeFields.interaction();
        return new PreventBlockPlaceAbility(entityActions, placeToOperation, placeOnOperation, placeToFilter, placeOnFilter, directions, interaction.heldItemFilter(), interaction.heldItemOperation(), interaction.resultItemOperation(), interaction.resultStack(), interaction.hands(), interaction.actionResult(), activeFields.priority(), properties, state, energyBarUsages);
    }));

    public final List<Action> entityActions;
    public final BlockOperation placeToOperation, placeOnOperation;
    public final BlockFilter placeToFilter, placeOnFilter;
    public final EnumSet<Direction> directions;

    public PreventBlockPlaceAbility(List<Action> entityActions, BlockOperation placeToOperation, BlockOperation placeOnOperation, BlockFilter placeToFilter, BlockFilter placeOnFilter, EnumSet<Direction> directions, ItemFilter heldItemFilter, ItemOperation heldItemOperation, ItemOperation resultItemOperation, Optional<ItemStack> resultStack, EnumSet<InteractionHand> hands, InteractionResult actionResult, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(heldItemFilter, heldItemOperation, resultItemOperation, resultStack, hands, actionResult, priority, properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.placeToOperation = placeToOperation;
        this.placeOnOperation = placeOnOperation;
        this.placeToFilter = placeToFilter;
        this.placeOnFilter = placeOnFilter;
        this.directions = directions;
    }

    public boolean doesPrevent(LivingEntity holder, ItemStack heldStack, InteractionHand hand, BlockPos toPos, BlockPos onPos, Direction direction) {
        if (!super.shouldRun(hand, heldStack) || !directions.contains(direction)) {
            return false;
        }

        if (!placeOnFilter.isEmpty() && !placeOnFilter.test(holder.level(), onPos)) {
            return false;
        }

        if (!placeToFilter.isEmpty() && !placeToFilter.test(holder.level(), toPos)) {
            return false;
        }

        return true;
    }

    public void runActions(LivingEntity holder, InteractionHand hand, BlockPos toPos, BlockPos onPos, Direction direction) {
        if (!placeOnOperation.operations().isEmpty()) placeOnOperation.run(holder.level(), onPos, Optional.of(direction));
        if (!placeToOperation.operations().isEmpty()) placeToOperation.run(holder.level(), toPos, Optional.of(direction));
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(holder));

        if (holder instanceof Player player) {
            performSourceItemStuff(player, hand);
        }
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_BLOCK_PLACE.get();
    }

    public static class Serializer extends AbilitySerializer<PreventBlockPlaceAbility> {

        @Override
        public MapCodec<PreventBlockPlaceAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventBlockPlaceAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Prevents the player from placing blocks.")
                    .addOptional("actions", TYPE_ACTION_LIST, "Action(s) performed on the player upon being prevented from placing a block.")
                    .addOptional("place_to_operations", BlockOperation.documentation(provider), "Block operations(s) performed at the position of the block the player tried to place.")
                    .addOptional("place_on_operations", BlockOperation.documentation(provider), "Block operations(s) performed on the block the player tried to place a block on.")
                    .addOptional("place_to_filters", BlockFilter.documentation(provider), "Block filter(s) the block at the position of the block the player tried to place must pass.")
                    .addOptional("place_on_filters", BlockFilter.documentation(provider), "Block filter(s) the block the player tried to place a block on must pass for the actions to perform.")
                    .addOptional("directions", KryptoniteDocumented.TYPE_DIRECTION, "If the actions should be performed if the player tried to place a block at the specified side(s) of a block.")
                    .addOptional("held_item_filters", ItemFilter.documentation(provider), "Item filters(s) the item the player has used to try to place a block must pass for the item operations to perform.")
                    .addOptional("held_item_operations", ItemOperation.documentation(provider), "Item operation(s) performed on the item the player has used to try to place a block.")
                    .addOptional("result_item_operations", ItemOperation.documentation(provider), "Item operation(s) performed on the item given to the player upon trying to place a block.")
                    .addOptional("result_stack", TYPE_ITEM_STACK, "The item stack given to the player upon trying to place a block.")
                    .addOptional("hands", KryptoniteDocumented.TYPE_INTERACTION_HAND, "If the actions should be performed if the player used the specified hands when trying to place a block.")
                    .addOptional("action_result", KryptoniteDocumented.TYPE_INTERACTION_RESULT, "Used to indicate the result of a certain action.")
                    .addOptional("priority", TYPE_INT, "The run priority of this ability. Higher priorities of this ability performed first.", 0)
                    .addExampleObject(new PreventBlockPlaceAbility(List.of(), BlockOperation.EMPTY, BlockOperation.EMPTY, BlockFilter.EMPTY, BlockFilter.EMPTY, EnumSet.allOf(Direction.class), ItemFilter.EMPTY, ItemOperation.EMPTY, ItemOperation.EMPTY, Optional.empty(), EnumSet.allOf(InteractionHand.class), InteractionResult.SUCCESS, 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventBlockPlaceAbility(List.of(new RunCommandAction(new ParsedCommands("tellraw @s {\"text\": \"Cannot place a block here!\", \"color\": \"red\"}"))), BlockOperation.EMPTY, BlockOperation.EMPTY, BlockFilter.EMPTY, BlockFilter.EMPTY, EnumSet.allOf(Direction.class), ItemFilter.EMPTY, ItemOperation.EMPTY, ItemOperation.EMPTY, Optional.empty(), EnumSet.allOf(InteractionHand.class), InteractionResult.SUCCESS, 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}