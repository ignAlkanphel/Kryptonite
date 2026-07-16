package net.alkanphel.kryptonite.power.logic.condition.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.ItemConditionContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.holdersets.OrHolderSet;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.List;

public record ItemItemCondition(HolderSet<Item> item) implements ItemCondition {

    public static final MapCodec<ItemItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("item").forGetter(ItemItemCondition::item)
    ).apply(instance, ItemItemCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemItemCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderSet(Registries.ITEM), ItemItemCondition::item,
            ItemItemCondition::new
    );

    @Override
    public boolean test(ItemConditionContext context) {
        Holder<Item> holder = context.stack().typeHolder();
        return item.contains(holder);
    }

    @Override
    public ItemConditionSerializer<ItemItemCondition> getSerializer() {
        return ItemConditionSerializers.ITEM.get();
    }

    public static class Serializer extends ItemConditionSerializer<ItemItemCondition> {

        @Override
        public MapCodec<ItemItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, ItemItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Item")
                    .setDescription("Checks if the item is of a certain type.")
                    .add("item", KryptoniteDocumented.TYPE_ITEM_TYPE_HOLDER_SET, "Item IDs or tags this item needs to pass the check.")
                    .addExampleObject(new ItemItemCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("apple"))))))
                    .addExampleObject(new ItemItemCondition(new OrHolderSet<>(List.of(provider.lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.ARROWS), HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("apple"))))))));
            }
        }

}