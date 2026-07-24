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
import java.util.Optional;

public record OrItemCondition(List<ItemCondition> itemConditions) implements ItemCondition {

    public static final MapCodec<OrItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemCondition.LIST_CODEC.optionalFieldOf("item_conditions").forGetter(c -> Optional.of(c.itemConditions())),
            ItemCondition.LIST_CODEC.optionalFieldOf("conditions").forGetter(c -> Optional.empty())
    ).apply(instance, (itemConditions, conditions) ->
            new OrItemCondition(itemConditions.orElseGet(() -> conditions.orElse(List.of())))
    ));

    @Override
    public boolean test(ItemConditionContext context) {
        for (ItemCondition itemCondition : this.itemConditions) {
            if (itemCondition.test(context)) {
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
                    .setDescription("Allows you to group multiple item conditions into one using the OR logic. At least one of the given item conditions must be true for this one to be true aswell. The namespace alias \"palladium:or\" is supported.")
                    .add("item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "List of item conditions. This field supports aliases: \"item_conditions\" & \"conditions\"");
        }
    }

}