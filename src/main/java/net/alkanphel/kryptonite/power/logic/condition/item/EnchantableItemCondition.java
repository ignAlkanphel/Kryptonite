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

public record EnchantableItemCondition() implements ItemCondition {

    public static final MapCodec<EnchantableItemCondition> CODEC = MapCodec.unit(EnchantableItemCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantableItemCondition> STREAM_CODEC = StreamCodec.unit(new EnchantableItemCondition());

    @Override
    public boolean test(ItemConditionContext context) {
        return context.stack().isEnchantable();
    }

    @Override
    public ItemConditionSerializer<EnchantableItemCondition> getSerializer() {
        return ItemConditionSerializers.ENCHANTABLE.get();
    }

    public static class Serializer extends ItemConditionSerializer<EnchantableItemCondition> {

        @Override
        public MapCodec<EnchantableItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, EnchantableItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Enchantable")
                    .setDescription("Checks if the item is enchantable.")
                    .addExampleObject(new EnchantableItemCondition());
        }
    }

}