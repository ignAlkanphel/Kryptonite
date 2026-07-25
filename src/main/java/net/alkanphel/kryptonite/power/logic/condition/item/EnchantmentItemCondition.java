package net.alkanphel.kryptonite.power.logic.condition.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteSettingType;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.ItemConditionContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;

import java.util.Optional;

public record EnchantmentItemCondition(Optional<Holder<Enchantment>> enchantment, NumberComparator comparator, Value compareTo) implements ItemCondition {

    public static final MapCodec<EnchantmentItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Enchantment.CODEC.optionalFieldOf("enchantment").forGetter(EnchantmentItemCondition::enchantment),
            NumberComparator.CODEC.fieldOf("comparator").forGetter(EnchantmentItemCondition::comparator),
            Value.CODEC.fieldOf("compare_to").forGetter(EnchantmentItemCondition::compareTo)
    ).apply(instance, EnchantmentItemCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentItemCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(Enchantment.STREAM_CODEC), EnchantmentItemCondition::enchantment,
            NumberComparator.STREAM_CODEC, EnchantmentItemCondition::comparator,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), EnchantmentItemCondition::compareTo,
            EnchantmentItemCondition::new
    );

    @Override
    public boolean test(ItemConditionContext context) {
        ItemEnchantments enchantments = context.stack().get(DataComponents.ENCHANTMENTS);
        if (enchantments == null) return false;

        int compare = Math.max(0, compareTo.getAsInt(null));

        if (enchantment.isPresent()) {
            return comparator.compare(enchantments.getLevel(enchantment.get()), compare);
        }

        for (var entry : enchantments.entrySet()) {
            if (comparator.compare(entry.getIntValue(), compare)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ItemConditionSerializer<EnchantmentItemCondition> getSerializer() {
        return ItemConditionSerializers.ENCHANTMENT.get();
    }

    public static class Serializer extends ItemConditionSerializer<EnchantmentItemCondition> {

        @Override
        public MapCodec<EnchantmentItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, EnchantmentItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Enchantment")
                    .setDescription("Checks and compares the enchantment level of an item.")
                    .add("enchantment", SettingType.simple("Enchantment"), "The enchantment to check. If omitted, checks any enchantment.")
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator used to compare the enchantment level.")
                    .add("compare_to", KryptoniteSettingType.intValueRange(0, Integer.MAX_VALUE), "The enchantment level to compare against.")
                    .addExampleObject(new EnchantmentItemCondition(Optional.of(provider.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS)), NumberComparator.GREATER_OR_EQUAL, new StaticValue(5)));
        }
    }

}