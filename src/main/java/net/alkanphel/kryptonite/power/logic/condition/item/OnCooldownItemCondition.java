package net.alkanphel.kryptonite.power.logic.condition.item;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.ItemConditionContext;
import net.alkanphel.kryptonite.util.apoli.access.EntityLinkedItemStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record OnCooldownItemCondition() implements ItemCondition {

    public static final MapCodec<OnCooldownItemCondition> CODEC = MapCodec.unit(OnCooldownItemCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, OnCooldownItemCondition> STREAM_CODEC = StreamCodec.unit(new OnCooldownItemCondition());

    @Override
    public boolean test(ItemConditionContext context) {
        var stack = context.stack();
        if (stack.isEmpty()) return false;

        if (((EntityLinkedItemStack) (Object) stack).kryptonite$getEntity() instanceof Player player) {
            return player.getCooldowns().isOnCooldown(stack);
        }

        return false;
    }

    @Override
    public ItemConditionSerializer<OnCooldownItemCondition> getSerializer() {
        return ItemConditionSerializers.ON_COOLDOWN.get();
    }

    public static class Serializer extends ItemConditionSerializer<OnCooldownItemCondition> {

        @Override
        public MapCodec<OnCooldownItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, OnCooldownItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("On Cooldown")
                    .setDescription("Checks if the item stack is on cooldown for the holding player.")
                    .addExampleObject(new OnCooldownItemCondition());
        }
    }

}