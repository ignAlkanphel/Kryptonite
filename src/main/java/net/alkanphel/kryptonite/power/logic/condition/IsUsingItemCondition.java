package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
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

import java.util.List;

public record IsUsingItemCondition(List<ItemCondition> itemConditions) implements Condition {

    public static final MapCodec<IsUsingItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemCondition.LIST_CODEC.optionalFieldOf("item_conditions", List.of()).forGetter(IsUsingItemCondition::itemConditions)
    ).apply(instance, IsUsingItemCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, IsUsingItemCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(ItemCondition.LIST_CODEC), IsUsingItemCondition::itemConditions,
            IsUsingItemCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        if (context.getEntity() instanceof LivingEntity livingEntity && livingEntity.isUsingItem()) {
            InteractionHand activeHand = livingEntity.getUsedItemHand();
            ItemStack stackInHand = livingEntity.getItemInHand(activeHand);

            return itemConditions.isEmpty() || ItemCondition.checkConditions(itemConditions, livingEntity.level(), stackInHand);
        }
        else {
            return false;
        }

    }

    @Override
    public ConditionSerializer<IsUsingItemCondition> getSerializer() {
        return KryptoniteConditionSerializers.IS_USING_ITEM.get();
    }

    public static class Serializer extends ConditionSerializer<IsUsingItemCondition> {

        @Override
        public MapCodec<IsUsingItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, IsUsingItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Is Using Item")
                    .setDescription("Checks if the entity is currently using an item (e.g. eating a food item, using shield or bow, etc.)")
                    .addOptional("item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "If specified, these conditions must be fulfilled for the item.")
                    .addExampleObject(new IsUsingItemCondition(List.of()));
        }
    }

}