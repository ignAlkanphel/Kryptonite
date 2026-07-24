package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;

public record AirCondition(NumberComparator comparator, Value compareTo) implements Condition {

    public static final MapCodec<AirCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberComparator.CODEC.fieldOf("comparator").forGetter(AirCondition::comparator),
            Value.CODEC.fieldOf("compare_to").forGetter(AirCondition::compareTo)
    ).apply(instance, AirCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AirCondition> STREAM_CODEC = StreamCodec.composite(
            NumberComparator.STREAM_CODEC, AirCondition::comparator,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), AirCondition::compareTo,
            AirCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        return comparator.compare(entity.getAirSupply(), compareTo.getAsInt(context));
    }

    @Override
    public ConditionSerializer<AirCondition> getSerializer() {
        return KryptoniteConditionSerializers.AIR.get();
    }

    public static class Serializer extends ConditionSerializer<AirCondition> {

        @Override
        public MapCodec<AirCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, AirCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Air")
                    .setDescription("Checks the current air supply of the entity.")
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator being used")
                    .add("compare_to", TYPE_VALUE, "The value that is being compared against")
                    .addExampleObject(new AirCondition(NumberComparator.GREATER_THAN, new StaticValue(11.0)));
        }
    }

}