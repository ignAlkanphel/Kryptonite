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

public record AndItemCondition(List<ItemCondition> itemConditions) implements ItemCondition {

    public static final MapCodec<AndItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemCondition.LIST_CODEC.optionalFieldOf("item_conditions").forGetter(c -> Optional.of(c.itemConditions())),
            ItemCondition.LIST_CODEC.optionalFieldOf("conditions").forGetter(c -> Optional.empty())
    ).apply(instance, (itemConditions, conditions) ->
            new AndItemCondition(itemConditions.orElseGet(() -> conditions.orElse(List.of())))
    ));

    @Override
    public boolean test(ItemConditionContext context) {
        for (ItemCondition itemCondition : this.itemConditions) {
            if (!itemCondition.test(context)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemConditionSerializer<AndItemCondition> getSerializer() {
        return ItemConditionSerializers.AND.get();
    }

    public static class Serializer extends ItemConditionSerializer<AndItemCondition> {

        @Override
        public MapCodec<AndItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, AndItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("AND")
                    .setDescription("Allows you to group multiple item conditions into one using the AND logic. All of the given item conditions must be true for this one to be true aswell. The namespace alias \"palladium:and\" is supported.")
                    .add("item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "List of item conditions. This field supports aliases: \"item_conditions\" & \"conditions\"");
        }
    }

}