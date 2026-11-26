package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.logic.filter.ItemFilter;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public record UsingItemCondition(ItemFilter itemFilter) implements Condition {

    public static final MapCodec<UsingItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemFilter.CODEC.optionalFieldOf("item_filters", ItemFilter.EMPTY).forGetter(UsingItemCondition::itemFilter)
    ).apply(instance, UsingItemCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, UsingItemCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(ItemFilter.CODEC), UsingItemCondition::itemFilter,
            UsingItemCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        if (context.getEntity() instanceof LivingEntity livingEntity && livingEntity.isUsingItem()) {
            InteractionHand activeHand = livingEntity.getUsedItemHand();
            ItemStack stackInHand = livingEntity.getItemInHand(activeHand);

            return itemFilter.test(livingEntity.level(), stackInHand);
        }

        return false;
    }

    @Override
    public ConditionSerializer<UsingItemCondition> getSerializer() {
        return KryptoniteConditionSerializers.USING_ITEM.get();
    }

    public static class Serializer extends ConditionSerializer<UsingItemCondition> {

        @Override
        public MapCodec<UsingItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, UsingItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Using Item")
                    .setDescription("Checks if the entity is currently using an item (e.g. eating a food item, using shield or bow, etc.)")
                    .addOptional("item_filters", ItemFilter.documentation(provider), "Item filter(s) the used item must pass.")
                    .addExampleObject(new UsingItemCondition(ItemFilter.EMPTY));
        }
    }

}