package net.alkanphel.kryptonite.registry;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class KryptoniteRegistryKeys {

    public static final ResourceKey<Registry<BlockActionSerializer<?>>> BLOCK_ACTION_SERIALIZER = createRegistryKey("block_action_serializer");

    private static <T> ResourceKey<Registry<T>> createRegistryKey(String name) {
        return ResourceKey.createRegistryKey(Kryptonite.id(name));
    }

}