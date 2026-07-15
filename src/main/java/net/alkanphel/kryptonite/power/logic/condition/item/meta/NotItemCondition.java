package net.alkanphel.kryptonite.power.logic.condition.item.meta;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.ItemConditionContext;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.List;

public record NotItemCondition(List<ItemCondition> conditions) implements ItemCondition {

    public static final MapCodec<NotItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemCondition.LIST_CODEC.fieldOf("conditions").forGetter(NotItemCondition::conditions)
    ).apply(instance, NotItemCondition::new));

    @Override
    public boolean test(ItemConditionContext context) {
        for (ItemCondition condition : this.conditions) {
            if (condition.test(context)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemConditionSerializer<NotItemCondition> getSerializer() {
        return ItemConditionSerializers.NOT.get();
    }

    public static class Serializer extends ItemConditionSerializer<NotItemCondition> {

        @Override
        public MapCodec<NotItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, NotItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("NOT")
                    .setDescription("Allows you to group multiple item conditions into one using the NOT logic. None of the given item conditions must be true for this one to be true aswell.")
                    .add("conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "List of item conditions");
        }
    }

}