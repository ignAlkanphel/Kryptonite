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

public record DestroySpeedBlockCondition(NumberComparator comparator, Value compareTo) implements BlockCondition {

    public static final MapCodec<DestroySpeedBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberComparator.CODEC.fieldOf("comparator").forGetter(DestroySpeedBlockCondition::comparator),
            Value.CODEC.fieldOf("compare_to").forGetter(DestroySpeedBlockCondition::compareTo)
    ).apply(instance, DestroySpeedBlockCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DestroySpeedBlockCondition> STREAM_CODEC = StreamCodec.composite(
            NumberComparator.STREAM_CODEC, DestroySpeedBlockCondition::comparator,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), DestroySpeedBlockCondition::compareTo,
            DestroySpeedBlockCondition::new
    );

    @Override
    public boolean test(BlockConditionContext context) {
        float destroySpeed = context.blockState().getDestroySpeed(context.level(), context.pos());
        float comparedValue = compareTo.getAsFloat(null);

        return comparator.compare(destroySpeed, comparedValue);
    }

    @Override
    public BlockConditionSerializer<DestroySpeedBlockCondition> getSerializer() {
        return BlockConditionSerializers.DESTROY_SPEED.get();
    }

    public static class Serializer extends BlockConditionSerializer<DestroySpeedBlockCondition> {

        @Override
        public MapCodec<DestroySpeedBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, DestroySpeedBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Destroy Speed")
                    .setDescription("Checks the destroy speed value of the block.")
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator being used")
                    .add("compare_to", TYPE_VALUE, "The value that is being compared against")
                    .addExampleObject(new DestroySpeedBlockCondition(NumberComparator.EQUALS, new StaticValue(1.5)));
        }
    }

}