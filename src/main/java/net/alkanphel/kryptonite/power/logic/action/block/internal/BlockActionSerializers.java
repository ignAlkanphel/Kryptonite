package net.alkanphel.kryptonite.power.logic.action.block.internal;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.registry.KryptoniteRegistryKeys;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockActionSerializers {

    public static final DeferredRegister<BlockActionSerializer<?>> BLOCK_ACTION_SERIALIZERS = DeferredRegister.create(KryptoniteRegistryKeys.BLOCK_ACTION_SERIALIZER, Kryptonite.MOD_ID);

}