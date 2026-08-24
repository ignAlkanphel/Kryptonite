package net.alkanphel.kryptonite.power;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.value.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.logic.value.ValueSerializer;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public class KryptoniteValueSerializers {
    public static final DeferredRegister<ValueSerializer<?>> VALUE_SERIALIZERS = DeferredRegister.create(PalladiumRegistryKeys.VALUE_SERIALIZER, Kryptonite.MOD_ID);

    public static final DeferredHolder<ValueSerializer<?>, AttributeValue.Serializer> ATTRIBUTE = VALUE_SERIALIZERS.register("attribute", AttributeValue.Serializer::new);

}