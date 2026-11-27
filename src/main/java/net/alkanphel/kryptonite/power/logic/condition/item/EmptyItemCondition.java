package net.alkanphel.kryptonite.power.logic.condition.item;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.ItemConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record EmptyItemCondition() implements ItemCondition {

    public static final MapCodec<EmptyItemCondition> CODEC = MapCodec.unit(EmptyItemCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, EmptyItemCondition> STREAM_CODEC = StreamCodec.unit(new EmptyItemCondition());

    @Override
    public boolean test(ItemConditionContext context) {
        return context.stack().isEmpty();
    }

    @Override
    public ItemConditionSerializer<EmptyItemCondition> getSerializer() {
        return ItemConditionSerializers.EMPTY.get();
    }

    public static class Serializer extends ItemConditionSerializer<EmptyItemCondition> {

        @Override
        public MapCodec<EmptyItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, EmptyItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Empty")
                    .setDescription("Checks if there is no item.")
                    .addExampleObject(new EmptyItemCondition());
        }
    }

}