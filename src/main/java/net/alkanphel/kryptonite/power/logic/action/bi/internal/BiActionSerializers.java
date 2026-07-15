package net.alkanphel.kryptonite.power.logic.action.bi.internal;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.registry.KryptoniteRegistryKeys;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BiActionSerializers {

    public static final DeferredRegister<BiActionSerializer<?>> BI_ACTION_SERIALIZERS = DeferredRegister.create(KryptoniteRegistryKeys.BI_ACTION_SERIALIZER, Kryptonite.MOD_ID);

}