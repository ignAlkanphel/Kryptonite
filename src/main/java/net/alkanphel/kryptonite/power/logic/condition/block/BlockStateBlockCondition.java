package net.alkanphel.kryptonite.power.logic.condition.block;

import com.mojang.serialization.Codec;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.util.NumberComparator;

import java.util.Optional;

// TODO Try converting to Value
public record BlockStateBlockCondition(String property, Optional<NumberComparator> comparator, Optional<Integer> compareTo, Optional<Boolean> boolValue, Optional<String> enumValue) implements BlockCondition {

    public static final MapCodec<BlockStateBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("property").forGetter(BlockStateBlockCondition::property),
            NumberComparator.CODEC.optionalFieldOf("comparator").forGetter(BlockStateBlockCondition::comparator),
            Codec.INT.optionalFieldOf("compare_to").forGetter(BlockStateBlockCondition::compareTo),
            Codec.BOOL.optionalFieldOf("value").forGetter(BlockStateBlockCondition::boolValue),
            Codec.STRING.optionalFieldOf("enum").forGetter(BlockStateBlockCondition::enumValue)
    ).apply(instance, BlockStateBlockCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockStateBlockCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, BlockStateBlockCondition::property,
            ByteBufCodecs.optional(ByteBufCodecs.fromCodecTrusted(NumberComparator.CODEC)), BlockStateBlockCondition::comparator,
            ByteBufCodecs.optional(ByteBufCodecs.INT), BlockStateBlockCondition::compareTo,
            ByteBufCodecs.optional(ByteBufCodecs.BOOL), BlockStateBlockCondition::boolValue,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), BlockStateBlockCondition::enumValue,
            BlockStateBlockCondition::new
    );

    @Override
    public boolean test(BlockConditionContext context) {
        BlockState blockState = context.blockState();
        var propValue = blockState.getProperties()
                .stream()
                .filter(prop -> prop.getName().equals(property))
                .map(blockState::getValue)
                .findFirst()
                .orElse(null);

        return switch (propValue) {
            case Enum<?> enumProp when enumValue.isPresent() ->
                    enumProp.name().equalsIgnoreCase(enumValue.get());
            case Boolean boolProp when boolValue.isPresent() ->
                    boolProp == boolValue.get();
            case Integer intProp when comparator.isPresent() && compareTo.isPresent() ->
                    comparator.get().compare(intProp, compareTo.get());
            case null, default ->
                    propValue != null;
        };
    }

    @Override
    public BlockConditionSerializer<BlockStateBlockCondition> getSerializer() {
        return BlockConditionSerializers.BLOCK_STATE.get();
    }

    public static class Serializer extends BlockConditionSerializer<BlockStateBlockCondition> {

        @Override
        public MapCodec<BlockStateBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, BlockStateBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Block State")
                    .setDescription("Checks a block state property of the block. If none of the expected fields are specified, this condition will just check if the block has the specified property.")
                    .add("property", TYPE_STRING, "The name of the property that will be checked. Examples are \"facing\" or \"age\". See: https://minecraft.wiki/w/Block_states#List_of_block_states")
                    .addOptional("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator used for integer properties.")
                    .addOptional("compare_to", TYPE_INT, "If specified, the integer at which the integer value of the specified property will be compared to. Only used if the specified property has an integer value.")
                    .addOptional("value", TYPE_BOOLEAN, "If specified, the boolean to compare to the boolean value of the specified property. Only used if the specified property has a boolean value.")
                    .addOptional("enum", TYPE_STRING, "If specified, the string at which the string value of the specified property will be compared to. Only used if the specified property has a string value.")
                    .addExampleObject(new BlockStateBlockCondition(BlockStateProperties.LAYERS.getName(), Optional.of(NumberComparator.GREATER_OR_EQUAL), Optional.of(4), Optional.empty(), Optional.empty()))
                    .addExampleObject(new BlockStateBlockCondition(BlockStateProperties.LIT.getName(), Optional.empty(), Optional.empty(), Optional.of(true), Optional.empty()))
                    .addExampleObject(new BlockStateBlockCondition(BlockStateProperties.AXIS.getName(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of("y")))
                    .addExampleObject(new BlockStateBlockCondition(BlockStateProperties.WATERLOGGED.getName(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
        }
    }

}