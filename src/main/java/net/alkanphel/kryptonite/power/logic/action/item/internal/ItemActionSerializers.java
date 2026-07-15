package net.alkanphel.kryptonite.power.logic.action.item.internal;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.registry.KryptoniteRegistryKeys;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemActionSerializers {

    public static final DeferredRegister<ItemActionSerializer<?>> ITEM_ACTION_SERIALIZERS = DeferredRegister.create(KryptoniteRegistryKeys.ITEM_ACTION_SERIALIZER, Kryptonite.MOD_ID);

}