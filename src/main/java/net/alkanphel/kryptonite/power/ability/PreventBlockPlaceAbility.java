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
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            BlockAction.LIST_CODEC.optionalFieldOf("place_to_actions", List.of()).forGetter(a -> a.placeToActions),
            BlockAction.LIST_CODEC.optionalFieldOf("place_on_actions", List.of()).forGetter(a -> a.placeOnActions),
            BlockCondition.LIST_CODEC.optionalFieldOf("place_to_conditions", List.of()).forGetter(a -> a.placeToConditions),
            BlockCondition.LIST_CODEC.optionalFieldOf("place_on_conditions", List.of()).forGetter(a -> a.placeOnConditions),
            Direction.CODEC.listOf().xmap(EnumSet::copyOf, List::copyOf).optionalFieldOf("directions", EnumSet.allOf(Direction.class)).forGetter(a -> a.directions),
            InteractionPrioritizedFields.CODEC.forGetter(a -> new InteractionPrioritizedFields(new InteractionFields(a.heldItemActions, a.heldItemConditions, a.resultItemActions, a.resultStack, a.hands, a.actionResult), a.getPriority())),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, (entityActions, placeToActions, placeOnActions, placeToConditions, placeOnConditions, directions, activeFields, properties, state, energyBarUsages) -> {
        InteractionFields interaction = activeFields.interaction();
        return new PreventBlockPlaceAbility(entityActions, placeToActions, placeOnActions, placeToConditions, placeOnConditions, directions, interaction.heldItemActions(), interaction.heldItemConditions(), interaction.resultItemActions(), interaction.resultStack(), interaction.hands(), interaction.actionResult(), activeFields.priority(), properties, state, energyBarUsages);
    }));

    public final List<Action> entityActions;
    public final List<BlockAction> placeToActions;
    public final List<BlockAction> placeOnActions;
    public final List<BlockCondition> placeToConditions;
    public final List<BlockCondition> placeOnConditions;
    public final EnumSet<Direction> directions;

    public PreventBlockPlaceAbility(List<Action> entityActions, List<BlockAction> placeToActions, List<BlockAction> placeOnActions, List<BlockCondition> placeToConditions, List<BlockCondition> placeOnConditions, EnumSet<Direction> directions, List<ItemAction> heldItemActions, List<ItemCondition> heldItemConditions, List<ItemAction> resultItemActions, Optional<ItemStack> resultStack, EnumSet<InteractionHand> hands, InteractionResult actionResult, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(heldItemActions, heldItemConditions, resultItemActions, resultStack, hands, actionResult, priority, properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.placeToActions = placeToActions;
        this.placeOnActions = placeOnActions;
        this.placeToConditions = placeToConditions;
        this.placeOnConditions = placeOnConditions;
        this.directions = directions;
    }

    public boolean doesPrevent(LivingEntity holder, ItemStack heldStack, InteractionHand hand, BlockPos toPos, BlockPos onPos, Direction direction) {
        if (!super.shouldRun(hand, heldStack) || !directions.contains(direction)) {
            return false;
        }

        if (!placeOnConditions.isEmpty() && !BlockCondition.checkConditions(placeOnConditions, holder.level(), onPos)) {
            return false;
        }

        if (!placeToConditions.isEmpty() && !BlockCondition.checkConditions(placeToConditions, holder.level(), toPos)) {
            return false;
        }

        return true;
    }

    public void runActions(LivingEntity holder, InteractionHand hand, BlockPos toPos, BlockPos onPos, Direction direction) {
        if (!placeOnActions.isEmpty()) BlockAction.runList(placeOnActions, holder.level(), onPos, Optional.of(direction));
        if (!placeToActions.isEmpty()) BlockAction.runList(placeToActions, holder.level(), toPos, Optional.of(direction));
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(holder));

        if (holder instanceof Player player) {
            performActorItemStuff(player, hand);
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
            builder.setName("Prevent Block Place")
                    .setDescription("Prevents the player from placing a block and runs actions upon being prevented.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "If specified, these entity actions will be run on the player upon being prevented from placing a block.")
                    .addOptional("place_to_actions", KryptoniteDocumented.TYPE_BLOCK_ACTION_LIST, "If specified, these actions will be run at the position of the block the player tried to place.")
                    .addOptional("place_on_actions", KryptoniteDocumented.TYPE_BLOCK_ACTION_LIST, "If specified, these actions will be run on the block the player tried to place a block on.")
                    .addOptional("place_to_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, the actions will only be run if the block at the position of the block the player tried to place fulfills these conditions.")
                    .addOptional("place_on_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, the specified actions will only be run if the block the player tried to place a block on fulfills these conditions.")
                    .addOptional("directions", KryptoniteDocumented.TYPE_DIRECTION, "If the specified actions should be run if the player tried to place a block at the specified side(s) of a block.")
                    .addOptional("held_item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "If specified, these actions will be run on the item the player has used to try to place a block.")
                    .addOptional("held_item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "If specified, the actions will only be run if these conditions are fulfilled by the item the player has used to try to place a block.")
                    .addOptional("result_item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "If specified, these actions will be run on the item that will be given to the player upon trying to place a block.")
                    .addOptional("result_stack", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "If specified, the item stack will be given to the player upon trying to place a block.")
                    .addOptional("hands", KryptoniteDocumented.TYPE_INTERACTION_HAND, "If the specified actions should be run if the player used the specified hands when trying to place a block.")
                    .addOptional("action_result", KryptoniteDocumented.TYPE_INTERACTION_RESULT, "Used to indicate the result of a certain action.")
                    .addOptional("priority", TYPE_INT, "The run priority of this ability. Higher priorities of this ability run first.", 0)
                    .addExampleObject(new PreventBlockPlaceAbility(List.of(), List.of(), List.of(), List.of(), List.of(), EnumSet.allOf(Direction.class), List.of(), List.of(), List.of(), Optional.empty(), EnumSet.allOf(InteractionHand.class), InteractionResult.SUCCESS, 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventBlockPlaceAbility(List.of(new RunCommandAction(new ParsedCommands("tellraw @s {\"text\": \"Cannot place a block here!\", \"color\": \"red\"}"))), List.of(), List.of(), List.of(), List.of(), EnumSet.allOf(Direction.class), List.of(), List.of(), List.of(), Optional.empty(), EnumSet.allOf(InteractionHand.class), InteractionResult.SUCCESS, 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}