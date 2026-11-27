package net.alkanphel.kryptonite.power.logic.action.block.internal;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.action.block.*;
import net.alkanphel.kryptonite.power.logic.action.block.OffsetBlockAction;
import net.alkanphel.kryptonite.registry.KryptoniteRegistryKeys;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockActionSerializers {

    public static final DeferredRegister<BlockActionSerializer<?>> BLOCK_ACTION_SERIALIZERS = DeferredRegister.create(KryptoniteRegistryKeys.BLOCK_ACTION_SERIALIZER, Kryptonite.MOD_ID);

    public static final DeferredHolder<BlockActionSerializer<?>, AreaOfEffectBlockAction.Serializer> AREA_OF_EFFECT = BLOCK_ACTION_SERIALIZERS.register("area_of_effect", AreaOfEffectBlockAction.Serializer::new);
    public static final DeferredHolder<BlockActionSerializer<?>, BoneMealBlockAction.Serializer> BONE_MEAL = BLOCK_ACTION_SERIALIZERS.register("bone_meal", BoneMealBlockAction.Serializer::new);
    public static final DeferredHolder<BlockActionSerializer<?>, CommandBlockAction.Serializer> COMMAND = BLOCK_ACTION_SERIALIZERS.register("command", CommandBlockAction.Serializer::new);
    public static final DeferredHolder<BlockActionSerializer<?>, ExplodeBlockAction.Serializer> EXPLODE = BLOCK_ACTION_SERIALIZERS.register("explode", ExplodeBlockAction.Serializer::new);
    public static final DeferredHolder<BlockActionSerializer<?>, ModifyStateBlockAction.Serializer> MODIFY_STATE = BLOCK_ACTION_SERIALIZERS.register("modify_state", ModifyStateBlockAction.Serializer::new);
    public static final DeferredHolder<BlockActionSerializer<?>, OffsetBlockAction.Serializer> OFFSET = BLOCK_ACTION_SERIALIZERS.register("offset", OffsetBlockAction.Serializer::new);
    public static final DeferredHolder<BlockActionSerializer<?>, PlacementBlockAction.Serializer> PLACEMENT = BLOCK_ACTION_SERIALIZERS.register("placement", PlacementBlockAction.Serializer::new);
    public static final DeferredHolder<BlockActionSerializer<?>, SummonBlockAction.Serializer> SUMMON = BLOCK_ACTION_SERIALIZERS.register("summon", SummonBlockAction.Serializer::new);

}