package net.alkanphel.kryptonite.power.logic.action.item.internal;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.action.item.*;
import net.alkanphel.kryptonite.registry.KryptoniteRegistryKeys;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemActionSerializers {

    public static final DeferredRegister<ItemActionSerializer<?>> ITEM_ACTION_SERIALIZERS = DeferredRegister.create(KryptoniteRegistryKeys.ITEM_ACTION_SERIALIZER, Kryptonite.MOD_ID);

    public static final DeferredHolder<ItemActionSerializer<?>, ConsumeItemAction.Serializer> CONSUME = ITEM_ACTION_SERIALIZERS.register("consume", ConsumeItemAction.Serializer::new);
    public static final DeferredHolder<ItemActionSerializer<?>, DamageItemAction.Serializer> DAMAGE = ITEM_ACTION_SERIALIZERS.register("damage", DamageItemAction.Serializer::new);

}