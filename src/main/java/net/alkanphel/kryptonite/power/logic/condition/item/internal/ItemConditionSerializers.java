package net.alkanphel.kryptonite.power.logic.condition.item.internal;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.condition.item.meta.*;
import net.alkanphel.kryptonite.registry.KryptoniteRegistryKeys;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemConditionSerializers {

    public static final DeferredRegister<ItemConditionSerializer<?>> ITEM_CONDITION_SERIALIZERS = DeferredRegister.create(KryptoniteRegistryKeys.ITEM_CONDITION_SERIALIZER, Kryptonite.MOD_ID);

    public static final DeferredHolder<ItemConditionSerializer<?>, AndItemCondition.Serializer> AND = ITEM_CONDITION_SERIALIZERS.register("and", AndItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, NotItemCondition.Serializer> NOT = ITEM_CONDITION_SERIALIZERS.register("not", NotItemCondition.Serializer::new);
    public static final DeferredHolder<ItemConditionSerializer<?>, OrItemCondition.Serializer> OR = ITEM_CONDITION_SERIALIZERS.register("or", OrItemCondition.Serializer::new);

}