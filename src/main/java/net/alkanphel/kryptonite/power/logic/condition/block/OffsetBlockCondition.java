package net.alkanphel.kryptonite.power.logic.condition.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

public record OffsetBlockCondition(BlockCondition condition, Value x, Value y, Value z) implements BlockCondition {

    public static final MapCodec<OffsetBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockCondition.CODEC.fieldOf("block_conditions").forGetter(OffsetBlockCondition::condition),
            Value.CODEC.optionalFieldOf("x", new StaticValue(0)).forGetter(OffsetBlockCondition::x),
            Value.CODEC.optionalFieldOf("y", new StaticValue(0)).forGetter(OffsetBlockCondition::y),
            Value.CODEC.optionalFieldOf("z", new StaticValue(0)).forGetter(OffsetBlockCondition::z)
    ).apply(instance, OffsetBlockCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, OffsetBlockCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(BlockCondition.CODEC), OffsetBlockCondition::condition,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), OffsetBlockCondition::x,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), OffsetBlockCondition::y,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), OffsetBlockCondition::z,
            OffsetBlockCondition::new
    );

    @Override
    public boolean test(BlockConditionContext context) {
        Level level = context.level();
        BlockPos offsetBlockPos = context.pos().offset(x.getAsInt(null), y.getAsInt(null), z.getAsInt(null));

        return level.hasChunkAt(offsetBlockPos) && condition.test(level, offsetBlockPos);
    }

    @Override
    public BlockConditionSerializer<OffsetBlockCondition> getSerializer() {
        return BlockConditionSerializers.OFFSET.get();
    }

    public static class Serializer extends BlockConditionSerializer<OffsetBlockCondition> {

        @Override
        public MapCodec<OffsetBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, OffsetBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Offset")
                    .setDescription("Checks block conditions at a position offset from the entity's current block position.")
                    .add("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "The block condition to check at the offset position.")
                    .addOptional("x", TYPE_VALUE, "How much to offset the position on the x-axis.", new StaticValue(0))
                    .addOptional("y", TYPE_VALUE, "How much to offset the position on the y-axis.", new StaticValue(0))
                    .addOptional("z", TYPE_VALUE, "How much to offset the position on the z-axis.", new StaticValue(0));
        }
    }

}