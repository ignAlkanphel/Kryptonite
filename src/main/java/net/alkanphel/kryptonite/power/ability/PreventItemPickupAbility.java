package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.mixin.common.ItemEntityAccessor;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.alkanphel.kryptonite.power.logic.condition.item.ItemItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.util.apoli.InventoryUtil;
import net.alkanphel.kryptonite.util.apoli.MiscUtil;
import net.alkanphel.kryptonite.util.apoli.ability.Prioritized;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.condition.AsSourceCondition;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.HasEffectCondition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PreventItemPickupAbility extends Ability implements Prioritized {

    public static final MapCodec<PreventItemPickupAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("target_actions_thrower", List.of()).forGetter(a -> a.targetActionsThrower),
            Action.LIST_CODEC.optionalFieldOf("target_actions_item", List.of()).forGetter(a -> a.targetActionsItem),
            Condition.CODEC.optionalFieldOf("target_conditions", TrueCondition.INSTANCE).forGetter(a -> a.targetConditions),
            ItemAction.LIST_CODEC.optionalFieldOf("item_actions", List.of()).forGetter(a -> a.itemActions),
            ItemCondition.LIST_CODEC.optionalFieldOf("item_conditions", List.of()).forGetter(a -> a.itemConditions),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(a -> a.priority),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventItemPickupAbility::new));

    private final List<Action> targetActionsThrower, targetActionsItem;
    private final List<ItemAction> itemActions;
    private final Condition targetConditions;
    private final List<ItemCondition> itemConditions;
    private final int priority;

    public PreventItemPickupAbility(List<Action> targetActionsThrower, List<Action> targetActionsItem, Condition targetConditions, List<ItemAction> itemActions, List<ItemCondition> itemConditions, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.targetActionsThrower = targetActionsThrower;
        this.targetActionsItem = targetActionsItem;
        this.itemActions = itemActions;
        this.targetConditions = targetConditions;
        this.itemConditions = itemConditions;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    public boolean doesPrevent(ItemStack stack, Entity thrower, Entity holder) {
        if (!itemConditions.isEmpty() && !ItemCondition.checkConditions(itemConditions, holder.level(), stack)) {
            return false;
        }

        return targetConditions.test(DataContext.forAffected(thrower, holder, null));
    }

    public static boolean doesPrevent(ItemEntity itemEntity, Entity holderEntity) {
        if (!(holderEntity instanceof LivingEntity livingEntity)) return false;

        ItemStack stack = itemEntity.getItem();
        EntityReference<Entity> throwerReference = ((ItemEntityAccessor) itemEntity).kryptonite$getThrowerUuid();
        UUID throwerUuid = throwerReference != null ? throwerReference.getUUID() : null;
        Entity throwerEntity = MiscUtil.getEntityByUuid(throwerUuid, holderEntity.level().getServer());

        List<PreventItemPickupAbility> abilities = Prioritized.collect(livingEntity, PreventItemPickupAbility.class,
                ability -> ability.doesPrevent(stack, throwerEntity, holderEntity));

        abilities.forEach(ability -> ability.runActions(itemEntity, throwerEntity, holderEntity));
        return !abilities.isEmpty();
    }

    public void runActions(ItemEntity itemEntity, Entity thrower, Entity holder) {
        SlotAccess slotAccess = InventoryUtil.createSlotAccess(itemEntity.getItem());

        if (!itemActions.isEmpty()) ItemAction.runList(itemActions, holder.level(), slotAccess);
        if (!targetActionsThrower.isEmpty()) Action.runList(targetActionsThrower, DataContext.forAffected(holder, thrower, null));
        if (!targetActionsItem.isEmpty()) Action.runList(targetActionsItem, DataContext.forAffected(itemEntity, holder, null));

        itemEntity.setItem(slotAccess.get());
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_ITEM_PICKUP.get();
    }

    public static class Serializer extends AbilitySerializer<PreventItemPickupAbility> {

        @Override
        public MapCodec<PreventItemPickupAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventItemPickupAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Prevents the entity that has this ability from picking up items. For this ability, the \"source\" is the entity that threw the item & the \"target\" is the entity attempting to pick up the item.")
                    .addOptional("target_actions_thrower", TYPE_ACTION_LIST, "Action(s) performed on the entity picking up the item.")
                    .addOptional("target_actions_item", TYPE_ACTION_LIST, "Action(s) performed on the item entity being picked up.")
                    .addOptional("target_conditions", TYPE_CONDITION_LIST, "Condition(s) the target must meet to prevent item pickup.")
                    .addOptional("item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "Item action(s) performed on the item stack being picked up.")
                    .addOptional("item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "Item condition(s) the item being picked up must meet to prevent item pickup.")
                    .addOptional("priority", TYPE_INT, "The run priority of this ability. Higher priorities run first.", 0)
                    .addExampleObject(new PreventItemPickupAbility(List.of(), List.of(), TrueCondition.INSTANCE, List.of(), List.of(), 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventItemPickupAbility(List.of(), List.of(), new AsSourceCondition(Collections.singletonList(new HasEffectCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, Identifier.withDefaultNamespace("speed")))))))), List.of(), List.of(new ItemItemCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("apple"))))))), 1, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}