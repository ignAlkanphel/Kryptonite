package net.alkanphel.kryptonite.power.logic.condition.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;

public record HeightBlockCondition(NumberComparator comparator, Value compareTo) implements BlockCondition {

    public static final MapCodec<HeightBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberComparator.CODEC.fieldOf("comparator").forGetter(HeightBlockCondition::comparator),
            Value.CODEC.fieldOf("compare_to").forGetter(HeightBlockCondition::compareTo)
    ).apply(instance, HeightBlockCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, HeightBlockCondition> STREAM_CODEC = StreamCodec.composite(
            NumberComparator.STREAM_CODEC, HeightBlockCondition::comparator,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), HeightBlockCondition::compareTo,
            HeightBlockCondition::new
    );

    @Override
    public boolean test(BlockConditionContext context) {
        int y = context.pos().getY();
        int comparedValue = compareTo.getAsInt(null);

        return comparator.compare(y, comparedValue);
    }

    @Override
    public BlockConditionSerializer<HeightBlockCondition> getSerializer() {
        return BlockConditionSerializers.HEIGHT.get();
    }

    public static class Serializer extends BlockConditionSerializer<HeightBlockCondition> {

        @Override
        public MapCodec<HeightBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, HeightBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Height")
                    .setDescription("Checks the Y position of the block.")
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator being used")
                    .add("compare_to", TYPE_VALUE, "The value that is being compared against")
                    .addExampleObject(new HeightBlockCondition(NumberComparator.GREATER_THAN, new StaticValue(64)));
        }
    }

}