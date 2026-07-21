package net.alkanphel.kryptonite.power.logic.condition.item.internal;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.condition.item.CooldownRelativeItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.*;
import net.alkanphel.kryptonite.power.logic.condition.item.meta.*;
import net.alkanphel.kryptonite.registry.KryptoniteRegistryKeys;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemConditionSerializers {

    public static final DeferredRegister<ItemConditionSerializer<?>> ITEM_CONDITION_SERIALIZERS = DeferredRegister.create(KryptoniteRegistryKeys.ITEM_CONDITION_SERIALIZER, Kryptonite.MOD_ID);

    public static final DeferredHolder<ItemConditionSerializer<?>, AndItemCondition.Serializer> AND = ITEM_CONDITION_SERIALIZERS.register("and", AndItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, NotItemCondition.Serializer> NOT = ITEM_CONDITION_SERIALIZERS.register("not", NotItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, OrItemCondition.Serializer> OR = ITEM_CONDITION_SERIALIZERS.register("or", OrItemCondition.Serializer::new);

    public static final DeferredHolder<ItemConditionSerializer<?>, AmountItemCondition.Serializer> AMOUNT = ITEM_CONDITION_SERIALIZERS.register("amount", AmountItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, BlockItemCondition.Serializer> BLOCK_ITEM = ITEM_CONDITION_SERIALIZERS.register("block_item", BlockItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, ComponentItemCondition.Serializer> COMPONENT = ITEM_CONDITION_SERIALIZERS.register("component", ComponentItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, CooldownItemCondition.Serializer> COOLDOWN = ITEM_CONDITION_SERIALIZERS.register("cooldown", CooldownItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, CooldownRelativeItemCondition.Serializer> COOLDOWN_RELATIVE = ITEM_CONDITION_SERIALIZERS.register("cooldown_relative", CooldownRelativeItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, DamageableItemCondition.Serializer> DAMAGEABLE = ITEM_CONDITION_SERIALIZERS.register("damageable", DamageableItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, DurabilityItemCondition.Serializer> DURABILITY = ITEM_CONDITION_SERIALIZERS.register("durability", DurabilityItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, DurabilityRelativeItemCondition.Serializer> DURABILITY_RELATIVE = ITEM_CONDITION_SERIALIZERS.register("durability_relative", DurabilityRelativeItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, EmptyItemCondition.Serializer> EMPTY = ITEM_CONDITION_SERIALIZERS.register("empty", EmptyItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, EnchantableItemCondition.Serializer> ENCHANTABLE = ITEM_CONDITION_SERIALIZERS.register("enchantable", EnchantableItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, EquipableItemCondition.Serializer> EQUIPABLE = ITEM_CONDITION_SERIALIZERS.register("equipable", EquipableItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, FuelItemCondition.Serializer> FUEL = ITEM_CONDITION_SERIALIZERS.register("fuel", FuelItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, HolderConditionItemCondition.Serializer> HOLDER_CONDITION = ITEM_CONDITION_SERIALIZERS.register("holder_condition", HolderConditionItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, ItemItemCondition.Serializer> ITEM = ITEM_CONDITION_SERIALIZERS.register("item", ItemItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, OnCooldownItemCondition.Serializer> ON_COOLDOWN = ITEM_CONDITION_SERIALIZERS.register("on_cooldown", OnCooldownItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, RepairableItemCondition.Serializer> REPAIRABLE = ITEM_CONDITION_SERIALIZERS.register("repairable", RepairableItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, SmeltableItemCondition.Serializer> SMELTABLE = ITEM_CONDITION_SERIALIZERS.register("smeltable", SmeltableItemCondition.Serializer::new);

}