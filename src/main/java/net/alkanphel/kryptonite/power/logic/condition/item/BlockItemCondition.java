package net.alkanphel.kryptonite.power.logic.condition.item;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.ItemConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BlockItem;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record BlockItemCondition() implements ItemCondition {

    public static final MapCodec<BlockItemCondition> CODEC = MapCodec.unit(BlockItemCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockItemCondition> STREAM_CODEC = StreamCodec.unit(new BlockItemCondition());

    @Override
    public boolean test(ItemConditionContext context) {
        return context.stack().getItem() instanceof BlockItem;
    }

    @Override
    public ItemConditionSerializer<BlockItemCondition> getSerializer() {
        return ItemConditionSerializers.BLOCK_ITEM.get();
    }

    public static class Serializer extends ItemConditionSerializer<BlockItemCondition> {

        @Override
        public MapCodec<BlockItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, BlockItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Block Item")
                    .setDescription("Checks if the item is an instanceof BlockItem (an item that can place blocks).")
                    .addExampleObject(new BlockItemCondition());
        }
    }

}