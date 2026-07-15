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

public record OrItemCondition(List<ItemCondition> conditions) implements ItemCondition {

    public static final MapCodec<OrItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemCondition.LIST_CODEC.fieldOf("conditions").forGetter(OrItemCondition::conditions)
    ).apply(instance, OrItemCondition::new));

    @Override
    public boolean test(ItemConditionContext context) {
        for (ItemCondition condition : this.conditions) {
            if (condition.test(context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemConditionSerializer<OrItemCondition> getSerializer() {
        return ItemConditionSerializers.OR.get();
    }

    public static class Serializer extends ItemConditionSerializer<OrItemCondition> {

        @Override
        public MapCodec<OrItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, OrItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("OR")
                    .setDescription("Allows you to group multiple item conditions into one using the OR logic. At least one of the given item conditions must be true for this one to be true aswell.")
                    .add("conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "List of item conditions");
        }
    }

}