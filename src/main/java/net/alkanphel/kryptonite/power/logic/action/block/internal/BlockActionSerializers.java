package net.alkanphel.kryptonite.power.logic.action.block.internal;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.action.block.*;
import net.alkanphel.kryptonite.registry.KryptoniteRegistryKeys;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockActionSerializers {

    public static final DeferredRegister<BlockActionSerializer<?>> BLOCK_ACTION_SERIALIZERS = DeferredRegister.create(KryptoniteRegistryKeys.BLOCK_ACTION_SERIALIZER, Kryptonite.MOD_ID);

    public static final DeferredHolder<BlockActionSerializer<?>, OffsetBlockAction.Serializer> OFFSET = BLOCK_ACTION_SERIALIZERS.register("offset", OffsetBlockAction.Serializer::new);

    public static final DeferredHolder<BlockActionSerializer<?>, AddBlockBlockAction.Serializer> ADD_BLOCK = BLOCK_ACTION_SERIALIZERS.register("add_block", AddBlockBlockAction.Serializer::new);
    public static final DeferredHolder<BlockActionSerializer<?>, AreaOfEffectBlockAction.Serializer> AREA_OF_EFFECT = BLOCK_ACTION_SERIALIZERS.register("area_of_effect", AreaOfEffectBlockAction.Serializer::new);
    public static final DeferredHolder<BlockActionSerializer<?>, BoneMealBlockAction.Serializer> BONE_MEAL = BLOCK_ACTION_SERIALIZERS.register("bone_meal", BoneMealBlockAction.Serializer::new);
    public static final DeferredHolder<BlockActionSerializer<?>, ExplodeBlockAction.Serializer> EXPLODE = BLOCK_ACTION_SERIALIZERS.register("explode", ExplodeBlockAction.Serializer::new);
    public static final DeferredHolder<BlockActionSerializer<?>, FallingBlockBlockAction.Serializer> FALLING_BLOCK = BLOCK_ACTION_SERIALIZERS.register("falling_block", FallingBlockBlockAction.Serializer::new);
    public static final DeferredHolder<BlockActionSerializer<?>, ModifyBlockStateBlockAction.Serializer> MODIFY_BLOCK_STATE = BLOCK_ACTION_SERIALIZERS.register("modify_block_state", ModifyBlockStateBlockAction.Serializer::new);
    public static final DeferredHolder<BlockActionSerializer<?>, RunCommandBlockAction.Serializer> RUN_COMMAND = BLOCK_ACTION_SERIALIZERS.register("run_command", RunCommandBlockAction.Serializer::new);
    public static final DeferredHolder<BlockActionSerializer<?>, SetBlockBlockAction.Serializer> SET_BLOCK = BLOCK_ACTION_SERIALIZERS.register("set_block", SetBlockBlockAction.Serializer::new);
    public static final DeferredHolder<BlockActionSerializer<?>, SpawnEntityBlockAction.Serializer> SPAWN_ENTITY = BLOCK_ACTION_SERIALIZERS.register("spawn_entity", SpawnEntityBlockAction.Serializer::new);

}