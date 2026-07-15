package net.alkanphel.kryptonite.power.logic.condition.item.internal;

import com.mojang.serialization.Codec;
import net.alkanphel.kryptonite.power.logic.condition.item.meta.AndItemCondition;
import net.alkanphel.kryptonite.power.logic.context.ItemConditionContext;
import net.alkanphel.kryptonite.registry.KryptoniteRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface ItemCondition {

    Codec<ItemCondition> DIRECT_CODEC = KryptoniteRegistries.ITEM_CONDITION_SERIALIZER.byNameCodec().dispatch(ItemCondition::getSerializer, ItemConditionSerializer::codec);
    Codec<List<ItemCondition>> LIST_CODEC = ExtraCodecs.compactListCodec(DIRECT_CODEC);
    Codec<ItemCondition> CODEC = ExtraCodecs.compactListCodec(DIRECT_CODEC).xmap(AndItemCondition::new, condition -> condition instanceof AndItemCondition(List<ItemCondition> conditions) ? conditions : Collections.singletonList(condition));

    boolean test(ItemConditionContext context);

    default boolean test(Level level, ItemStack stack) {
        return test(new ItemConditionContext(level, stack));
    }

    static boolean checkConditions(Collection<ItemCondition> conditions, Level level, ItemStack stack) {
        return checkConditions(conditions, new ItemConditionContext(level, stack));
    }

    static boolean checkConditions(Collection<ItemCondition> conditions, ItemConditionContext context) {
        for (ItemCondition condition : conditions) {
            if (!condition.test(context)) {
                return false;
            }
        }

        return true;
    }

    ItemConditionSerializer<?> getSerializer();

}