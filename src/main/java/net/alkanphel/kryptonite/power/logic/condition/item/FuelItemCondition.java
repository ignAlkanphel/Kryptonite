package net.alkanphel.kryptonite.power.logic.condition.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteSettingType;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.ItemConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;

public record FuelItemCondition(NumberComparator comparator, Value compareTo) implements ItemCondition {

    public static final MapCodec<FuelItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberComparator.CODEC.optionalFieldOf("comparator", NumberComparator.GREATER_THAN).forGetter(FuelItemCondition::comparator),
            Value.CODEC.fieldOf("compare_to").forGetter(FuelItemCondition::compareTo)
    ).apply(instance, FuelItemCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FuelItemCondition> STREAM_CODEC = StreamCodec.composite(
            NumberComparator.STREAM_CODEC, FuelItemCondition::comparator,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), FuelItemCondition::compareTo,
            FuelItemCondition::new
    );

    @Override
    public boolean test(ItemConditionContext context) {
        ItemStack stack = context.stack();
        Level level = context.level();

        return comparator.compare(stack.getBurnTime(null, level.fuelValues()), Math.max(0, compareTo.getAsInt(null)));
    }

    @Override
    public ItemConditionSerializer<FuelItemCondition> getSerializer() {
        return ItemConditionSerializers.FUEL.get();
    }

    public static class Serializer extends ItemConditionSerializer<FuelItemCondition> {

        @Override
        public MapCodec<FuelItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, FuelItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Fuel")
                    .setDescription("Checks if the item is considered as fuel.")
                    .addOptional("comparator", TYPE_NUMBER_COMPARATOR, "The fuel time value (in ticks) of the item stack should be compared to the value.", NumberComparator.GREATER_THAN)
                    .add("compare_to", KryptoniteSettingType.intValueRange(0, Integer.MAX_VALUE), "The value at which the fuel time value (in ticks) of the item stack will be compared to.")
                    .addExampleObject(new FuelItemCondition(NumberComparator.GREATER_OR_EQUAL, new StaticValue(10)));
        }
    }

}