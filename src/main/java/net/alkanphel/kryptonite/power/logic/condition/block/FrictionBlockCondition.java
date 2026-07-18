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

public record FrictionBlockCondition(NumberComparator comparator, Value compareTo) implements BlockCondition {

    public static final MapCodec<FrictionBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberComparator.CODEC.fieldOf("comparator").forGetter(FrictionBlockCondition::comparator),
            Value.CODEC.fieldOf("compare_to").forGetter(FrictionBlockCondition::compareTo)
    ).apply(instance, FrictionBlockCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FrictionBlockCondition> STREAM_CODEC = StreamCodec.composite(
            NumberComparator.STREAM_CODEC, FrictionBlockCondition::comparator,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), FrictionBlockCondition::compareTo,
            FrictionBlockCondition::new
    );

    @Override
    public boolean test(BlockConditionContext context) {
        float friction = context.blockState().getBlock().getFriction();
        float comparedValue = compareTo.getAsFloat(null);

        return comparator.compare(friction, comparedValue);
    }

    @Override
    public BlockConditionSerializer<FrictionBlockCondition> getSerializer() {
        return BlockConditionSerializers.FRICTION.get();
    }

    public static class Serializer extends BlockConditionSerializer<FrictionBlockCondition> {

        @Override
        public MapCodec<FrictionBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, FrictionBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Friction")
                    .setDescription("Checks the friction value of the block.")
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "Comparison operator being used")
                    .add("compare_to", TYPE_VALUE, "Value that is being compared against")
                    .addExampleObject(new FrictionBlockCondition(NumberComparator.EQUALS, new StaticValue(0.98)));
        }
    }

}