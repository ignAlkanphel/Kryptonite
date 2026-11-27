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

public record CooldownOnItemCondition() implements ItemCondition {

    public static final MapCodec<CooldownOnItemCondition> CODEC = MapCodec.unit(CooldownOnItemCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, CooldownOnItemCondition> STREAM_CODEC = StreamCodec.unit(new CooldownOnItemCondition());

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
    public ItemConditionSerializer<CooldownOnItemCondition> getSerializer() {
        return ItemConditionSerializers.COOLDOWN_ON.get();
    }

    public static class Serializer extends ItemConditionSerializer<CooldownOnItemCondition> {

        @Override
        public MapCodec<CooldownOnItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, CooldownOnItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Cooldown On")
                    .setDescription("Checks if the cooldown is on for the held item stack of the player.")
                    .addExampleObject(new CooldownOnItemCondition());
        }
    }

}