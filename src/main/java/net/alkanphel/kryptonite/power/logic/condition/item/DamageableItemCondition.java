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

public record DamageableItemCondition() implements ItemCondition {

    public static final MapCodec<DamageableItemCondition> CODEC = MapCodec.unit(DamageableItemCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, DamageableItemCondition> STREAM_CODEC = StreamCodec.unit(new DamageableItemCondition());

    @Override
    public boolean test(ItemConditionContext context) {
        return context.stack().isDamageableItem();
    }

    @Override
    public ItemConditionSerializer<DamageableItemCondition> getSerializer() {
        return ItemConditionSerializers.DAMAGEABLE.get();
    }

    public static class Serializer extends ItemConditionSerializer<DamageableItemCondition> {

        @Override
        public MapCodec<DamageableItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, DamageableItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Damageable")
                    .setDescription("Checks if the item is able to be damaged.")
                    .addExampleObject(new DamageableItemCondition());
        }
    }

}