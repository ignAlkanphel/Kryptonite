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
            Action.LIST_CODEC.optionalFieldOf("entity_actions_thrower", List.of()).forGetter(a -> a.entityActionsThrower),
            Action.LIST_CODEC.optionalFieldOf("entity_actions_item", List.of()).forGetter(a -> a.entityActionsItem),
            ItemAction.LIST_CODEC.optionalFieldOf("item_actions", List.of()).forGetter(a -> a.itemActions),
            Condition.CODEC.optionalFieldOf("entity_conditions", TrueCondition.INSTANCE).forGetter(a -> a.entityConditions),
            ItemCondition.LIST_CODEC.optionalFieldOf("item_conditions", List.of()).forGetter(a -> a.itemConditions),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(a -> a.priority),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventItemPickupAbility::new));

    private final List<Action> entityActionsThrower, entityActionsItem;
    private final List<ItemAction> itemActions;
    private final Condition entityConditions;
    private final List<ItemCondition> itemConditions;
    private final int priority;

    public PreventItemPickupAbility(List<Action> entityActionsThrower, List<Action> entityActionsItem, List<ItemAction> itemActions, Condition entityConditions, List<ItemCondition> itemConditions, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActionsThrower = entityActionsThrower;
        this.entityActionsItem = entityActionsItem;
        this.itemActions = itemActions;
        this.entityConditions = entityConditions;
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

        return entityConditions.test(DataContext.forAffected(thrower, holder, null));
    }

    public static boolean doesPrevent(ItemEntity itemEntity, Entity holderEntity) {
        if (!(holderEntity instanceof LivingEntity livingEntity)) return false;

        ItemStack stack = itemEntity.getItem();
        EntityReference<Entity> throwerReference = ((ItemEntityAccessor) itemEntity).kryptonite$getThrowerUuid();

        UUID throwerUuid = null;
        if (throwerReference != null) throwerUuid = throwerReference.getUUID();

        Entity throwerEntity = MiscUtil.getEntityByUuid(throwerUuid, holderEntity.level().getServer());

        Prioritized.CallInstance<PreventItemPickupAbility> callInstance = new Prioritized.CallInstance<>();
        callInstance.add(livingEntity, PreventItemPickupAbility.class, ability -> ability.doesPrevent(stack, throwerEntity, holderEntity));

        if (callInstance.isEmpty()) return false;

        callInstance.forEachByPriority(p -> p.runActions(itemEntity, throwerEntity, holderEntity));
        return true;
    }

    public void runActions(ItemEntity itemEntity, Entity thrower, Entity holder) {
        SlotAccess stackReference = InventoryUtil.createStackReference(itemEntity.getItem());

        if (!itemActions.isEmpty()) ItemAction.runList(itemActions, holder.level(), stackReference);
        if (!entityActionsThrower.isEmpty()) Action.runList(entityActionsThrower, DataContext.forAffected(holder, thrower, null));
        if (!entityActionsItem.isEmpty()) Action.runList(entityActionsItem, DataContext.forAffected(itemEntity, holder, null));

        itemEntity.setItem(stackReference.get());
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
            builder.setName("Prevent Item Pickup")
                    .setDescription("Prevents the entity that has this ability from picking up items. For this ability, the \"source\" is the entity that threw the item & the \"target\" is the entity attempting to pick up the item.")
                    .addOptional("entity_actions_thrower", TYPE_ACTION_LIST, "Action(s) performed on the entity picking up the item.")
                    .addOptional("entity_actions_item", TYPE_ACTION_LIST, "Action(s) performed on the item entity being picked up.")
                    .addOptional("item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "Item action(s) performed on the item stack being picked up.")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) the target must meet to prevent item pickup.")
                    .addOptional("item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "Item condition(s) the item being picked up must meet to prevent item pickup.")
                    .addOptional("priority", TYPE_INT, "The run priority of this ability. Higher priorities of this ability run first.", 0)
                    .addExampleObject(new PreventItemPickupAbility(List.of(), List.of(), List.of(), TrueCondition.INSTANCE, List.of(), 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventItemPickupAbility(List.of(), List.of(), List.of(), new AsSourceCondition(Collections.singletonList(new HasEffectCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, Identifier.withDefaultNamespace("speed")))))))), List.of(new ItemItemCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("apple"))))))), 1, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}