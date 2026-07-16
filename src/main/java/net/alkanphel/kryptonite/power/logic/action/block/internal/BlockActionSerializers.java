package net.alkanphel.kryptonite.power.logic.action.block.internal;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.action.block.*;
import net.alkanphel.kryptonite.registry.KryptoniteRegistryKeys;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockActionSerializers {

    public static final DeferredRegister<BlockActionSerializer<?>> BLOCK_ACTION_SERIALIZERS = DeferredRegister.create(KryptoniteRegistryKeys.BLOCK_ACTION_SERIALIZER, Kryptonite.MOD_ID);

    public static final DeferredHolder<BlockActionSerializer<?>, AddBlockBlockAction.Serializer> ADD_BLOCK = BLOCK_ACTION_SERIALIZERS.register("add_block", AddBlockBlockAction.Serializer::new);
    public static final DeferredHolder<BlockActionSerializer<?>, BoneMealBlockAction.Serializer> BONE_MEAL = BLOCK_ACTION_SERIALIZERS.register("bone_meal", BoneMealBlockAction.Serializer::new);
    public static final DeferredHolder<BlockActionSerializer<?>, SetBlockBlockAction.Serializer> SET_BLOCK = BLOCK_ACTION_SERIALIZERS.register("set_block", SetBlockBlockAction.Serializer::new);

}