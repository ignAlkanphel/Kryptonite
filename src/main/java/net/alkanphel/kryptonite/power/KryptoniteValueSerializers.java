package net.alkanphel.kryptonite.power;

import net.alkanphel.kryptonite.Kryptonite;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.logic.value.ValueSerializer;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public class KryptoniteValueSerializers {
    public static final DeferredRegister<ValueSerializer<?>> VALUE_SERIALIZERS = DeferredRegister.create(PalladiumRegistryKeys.VALUE_SERIALIZER, Kryptonite.MOD_ID);

}