package net.alkanphel.kryptonite.registry;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class KryptoniteRegistryKeys {

    public static final ResourceKey<Registry<DimensionConditionSerializer<?>>> DIMENSION_CONDITION_SERIALIZER = createRegistryKey("dimension_condition_serializer");

    private static <T> ResourceKey<Registry<T>> createRegistryKey(String name) {
        return ResourceKey.createRegistryKey(Kryptonite.id(name));
    }

}