package net.alkanphel.kryptonite.power.logic.condition.item;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.ItemConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record RepairableItemCondition() implements ItemCondition {

    public static final MapCodec<RepairableItemCondition> CODEC = MapCodec.unit(RepairableItemCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, RepairableItemCondition> STREAM_CODEC = StreamCodec.unit(new RepairableItemCondition());

    public boolean test(ItemConditionContext context) {
        ItemStack stack = context.stack();
        return stack.isValidRepairItem(stack);
    }

    @Override
    public ItemConditionSerializer<RepairableItemCondition> getSerializer() {
        return ItemConditionSerializers.REPAIRABLE.get();
    }

    public static class Serializer extends ItemConditionSerializer<RepairableItemCondition> {

        @Override
        public MapCodec<RepairableItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, RepairableItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Repairable")
                    .setDescription("Checks if the item is able to be repaired.")
                    .addExampleObject(new RepairableItemCondition());
        }
    }

}