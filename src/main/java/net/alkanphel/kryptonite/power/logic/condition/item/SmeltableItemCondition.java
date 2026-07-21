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
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record SmeltableItemCondition() implements ItemCondition {

    public static final MapCodec<SmeltableItemCondition> CODEC = MapCodec.unit(SmeltableItemCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, SmeltableItemCondition> STREAM_CODEC = StreamCodec.unit(new SmeltableItemCondition());

    @Override
    public boolean test(ItemConditionContext context) {
        ItemStack stack = context.stack();
        Level level = context.level();

        return level.recipeAccess() instanceof RecipeManager recipeManager
                && recipeManager.getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level).isPresent();
    }

    @Override
    public ItemConditionSerializer<SmeltableItemCondition> getSerializer() {
        return ItemConditionSerializers.SMELTABLE.get();
    }

    public static class Serializer extends ItemConditionSerializer<SmeltableItemCondition> {

        @Override
        public MapCodec<SmeltableItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, SmeltableItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Smeltable")
                    .setDescription("Checks if the item stack is an ingredient used in a smelting recipe.")
                    .addExampleObject(new SmeltableItemCondition());
        }
    }

}