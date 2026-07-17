package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.mixin.common.ItemEntityAccessor;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.meta.ActorConditionBiCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.ItemItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
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
import net.threetag.palladium.logic.condition.HasEffectCondition;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;

import java.util.List;
import java.util.UUID;

public class PreventItemPickupAbility extends Ability implements Prioritized {

    public static final MapCodec<PreventItemPickupAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions_thrower", List.of()).forGetter(a -> a.biEntityActionsThrower),
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions_item", List.of()).forGetter(a -> a.biEntityActionsItem),
            ItemAction.LIST_CODEC.optionalFieldOf("item_actions", List.of()).forGetter(a -> a.itemActions),
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(a -> a.biEntityConditions),
            ItemCondition.LIST_CODEC.optionalFieldOf("item_conditions", List.of()).forGetter(a -> a.itemConditions),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(a -> a.priority),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventItemPickupAbility::new));

    private final List<BiAction> biEntityActionsThrower, biEntityActionsItem;
    private final List<ItemAction> itemActions;
    private final List<BiCondition> biEntityConditions;
    private final List<ItemCondition> itemConditions;
    private final int priority;

    public PreventItemPickupAbility(List<BiAction> biEntityActionsThrower, List<BiAction> biEntityActionsItem, List<ItemAction> itemActions, List<BiCondition> biEntityConditions, List<ItemCondition> itemConditions, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.biEntityActionsThrower = biEntityActionsThrower;
        this.biEntityActionsItem = biEntityActionsItem;
        this.itemActions = itemActions;
        this.biEntityConditions = biEntityConditions;
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

        if (!biEntityConditions.isEmpty() && !BiCondition.checkConditions(biEntityConditions, holder, thrower)) {
            return false;
        }

        return true;
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
        SlotAccess stackReference = MiscUtil.createStackReference(itemEntity.getItem());

        if (!itemActions.isEmpty()) ItemAction.runList(itemActions, holder.level(), stackReference);
        if (!biEntityActionsThrower.isEmpty()) BiAction.runList(biEntityActionsThrower, thrower, holder);
        if (!biEntityActionsItem.isEmpty()) BiAction.runList(biEntityActionsItem, holder, itemEntity);

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
                    .setDescription("Prevents the entity that has this ability from picking up items.")
                    .addOptional("bientity_actions_thrower", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "If specified, these bi actions will be run on either or both actor & target entities. In the context of this field, the \"actor\" is the entity that threw the item & \"target\" the entity about to pick up the item.")
                    .addOptional("bientity_actions_item", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "If specified, these bi actions will be run on either or both actor & target entities. In the context of this field, the \"actor\" is the entity about to pick up the item & \"target\" the item entity to be picked up. ")
                    .addOptional("item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "If specified, these item actions will be run on the item that was attempted to be picked up.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, prevention will only happen if these bi conditions are fulfilled by either or both the actor & target entities. In the context of this field, the \"actor\" is the entity that threw the item & \"target\" the entity about to pick up the item.")
                    .addOptional("item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "If specified, prevention will only happen if these item conditions are fulfilled by the item about to be picked up.")
                    .addOptional("priority", TYPE_INT, "The run priority of this ability. Higher priorities of this ability run first.", 0)
                    .addExampleObject(new PreventItemPickupAbility(List.of(), List.of(), List.of(), List.of(), List.of(), 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventItemPickupAbility(List.of(), List.of(), List.of(), List.of(new ActorConditionBiCondition(new HasEffectCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, Identifier.withDefaultNamespace("speed")))))))), List.of(new ItemItemCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("apple")))))), 1, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}