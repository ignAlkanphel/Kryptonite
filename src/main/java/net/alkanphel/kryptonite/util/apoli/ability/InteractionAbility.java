package net.alkanphel.kryptonite.util.apoli.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.util.KryptoniteCodecs;
import net.alkanphel.kryptonite.util.apoli.InventoryUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class InteractionAbility extends Ability {

    public record InteractionFields(List<ItemAction> heldItemActions, List<ItemCondition> heldItemConditions, List<ItemAction> resultItemActions, Optional<ItemStack> resultStack, EnumSet<InteractionHand> hands, InteractionResult actionResult) {
        public static final MapCodec<InteractionFields> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemAction.LIST_CODEC.optionalFieldOf("held_item_actions", List.of()).forGetter(InteractionFields::heldItemActions),
                ItemCondition.LIST_CODEC.optionalFieldOf("held_item_conditions", List.of()).forGetter(InteractionFields::heldItemConditions),
                ItemAction.LIST_CODEC.optionalFieldOf("result_item_actions", List.of()).forGetter(InteractionFields::resultItemActions),
                ItemStack.CODEC.optionalFieldOf("result_stack").forGetter(InteractionFields::resultStack),
                Codec.list(KryptoniteCodecs.HAND_CODEC).xmap(EnumSet::copyOf, List::copyOf).optionalFieldOf("hands", EnumSet.allOf(InteractionHand.class)).forGetter(InteractionFields::hands),
                InteractionResultUtil.INTERACTION_RESULT_CODEC.optionalFieldOf("action_result", InteractionResult.SUCCESS).forGetter(InteractionFields::actionResult)
        ).apply(instance, InteractionFields::new));
    }

    public final List<ItemAction> heldItemActions;
    public final List<ItemCondition> heldItemConditions;
    public final List<ItemAction> resultItemActions;
    public final Optional<ItemStack> resultStack;
    public final EnumSet<InteractionHand> hands;
    public final InteractionResult actionResult;

    public InteractionAbility(List<ItemAction> heldItemActions, List<ItemCondition> heldItemConditions, List<ItemAction> resultItemActions, Optional<ItemStack> resultStack, EnumSet<InteractionHand> hands, InteractionResult actionResult, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.heldItemActions = heldItemActions;
        this.heldItemConditions = heldItemConditions;
        this.resultItemActions = resultItemActions;
        this.resultStack = resultStack;
        this.hands = hands;
        this.actionResult = actionResult;
    }

    public boolean shouldRun(InteractionHand hand, ItemStack heldStack) {
        return doesApplyToHand(hand) && doesApplyToItem(heldStack);
    }

    public boolean doesApplyToHand(InteractionHand hand) {
        return hands.contains(hand);
    }

    public boolean doesApplyToItem(ItemStack heldStack) {
        if (heldItemConditions.isEmpty()) return true;
        return ItemCondition.checkConditions(heldItemConditions, null, heldStack);
    }

    public InteractionResult getActionResult() {
        return actionResult;
    }

    protected void performActorItemStuff(Player actor, InteractionHand hand) {
        SlotAccess heldStackReference = getHeldStackReference(actor, hand);
        Level level = actor.level();

        if (!heldItemActions.isEmpty()) {
            ItemAction.runList(heldItemActions, level, heldStackReference);
        }

        ItemStack resultStack = this.resultStack.isPresent() ? this.resultStack.get().copy() : heldStackReference.get().copy();
        SlotAccess resultStackReference = InventoryUtil.createStackReference(resultStack);

        boolean modified = this.resultStack.isPresent() || !resultItemActions.isEmpty();

        if (!resultItemActions.isEmpty()) {
            ItemAction.runList(resultItemActions, level, resultStackReference);
        }

        if (modified) {
            if (heldStackReference.get().isEmpty()) {
                actor.setItemInHand(hand, resultStackReference.get());
            }
            else {
                actor.getInventory().placeItemBackInInventory(resultStackReference.get());
            }
        }
    }

    protected static SlotAccess getHeldStackReference(Player player, InteractionHand hand) {
        Inventory inventory = player.getInventory();
        int selectedSlot = inventory.getSelectedSlot();

        if (hand == InteractionHand.MAIN_HAND && Inventory.isHotbarSlot(selectedSlot)) {
            return SlotAccess.of(inventory::getSelectedItem, inventory::setSelectedItem);
        }
        else if (hand == InteractionHand.OFF_HAND) {
            return SlotAccess.forEquipmentSlot(player, EquipmentSlot.OFFHAND);
        }
        else {
            return SlotAccess.of(() -> ItemStack.EMPTY, stack -> {});
        }
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return null;
    }

}