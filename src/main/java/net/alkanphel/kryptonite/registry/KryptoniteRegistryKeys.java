package net.alkanphel.kryptonite.registry;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemActionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class KryptoniteRegistryKeys {

    public static final ResourceKey<Registry<BiActionSerializer<?>>> BI_ACTION_SERIALIZER = createRegistryKey("bi_action_serializer");
    public static final ResourceKey<Registry<BlockActionSerializer<?>>> BLOCK_ACTION_SERIALIZER = createRegistryKey("block_action_serializer");
    public static final ResourceKey<Registry<ItemActionSerializer<?>>> ITEM_ACTION_SERIALIZER = createRegistryKey("item_action_serializer");
    public static final ResourceKey<Registry<BiConditionSerializer<?>>> BI_CONDITION_SERIALIZER = createRegistryKey("bi_condition_serializer");
    public static final ResourceKey<Registry<BlockConditionSerializer<?>>> BLOCK_CONDITION_SERIALIZER = createRegistryKey("block_condition_serializer");
    public static final ResourceKey<Registry<ItemConditionSerializer<?>>> ITEM_CONDITION_SERIALIZER = createRegistryKey("item_condition_serializer");
    public static final ResourceKey<Registry<DimensionConditionSerializer<?>>> DIMENSION_CONDITION_SERIALIZER = createRegistryKey("dimension_condition_serializer");
    public static final ResourceKey<Registry<DamageConditionSerializer<?>>> DAMAGE_CONDITION_SERIALIZER = createRegistryKey("damage_condition_serializer");

    private static <T> ResourceKey<Registry<T>> createRegistryKey(String name) {
        return ResourceKey.createRegistryKey(Kryptonite.id(name));
    }

}