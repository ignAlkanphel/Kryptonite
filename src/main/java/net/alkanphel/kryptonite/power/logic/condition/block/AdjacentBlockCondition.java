package net.alkanphel.kryptonite.power.logic.condition.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.util.NumberComparator;

public record AdjacentBlockCondition(BlockCondition adjacentCondition, NumberComparator comparator, float compareTo) implements BlockCondition {

    public static final MapCodec<AdjacentBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockCondition.CODEC.fieldOf("adjacent_conditions").forGetter(AdjacentBlockCondition::adjacentCondition),
            NumberComparator.CODEC.fieldOf("comparator").forGetter(AdjacentBlockCondition::comparator),
            Codec.FLOAT.fieldOf("compare_to").forGetter(AdjacentBlockCondition::compareTo)
    ).apply(instance, AdjacentBlockCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AdjacentBlockCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(BlockCondition.CODEC), AdjacentBlockCondition::adjacentCondition,
            NumberComparator.STREAM_CODEC, AdjacentBlockCondition::comparator,
            ByteBufCodecs.FLOAT, AdjacentBlockCondition::compareTo,
            AdjacentBlockCondition::new
    );

    @Override
    public boolean test(BlockConditionContext context) {
        Level level = context.level();
        BlockPos pos = context.pos();

        int matches = 0;
        for (Direction direction : Direction.values()) {
            BlockPos offsetPos = pos.relative(direction);
            if (level.hasChunkAt(offsetPos) && adjacentCondition.test(level, offsetPos)) {
                matches++;
            }
        }

        return comparator.compare(matches, compareTo);
    }

    @Override
    public BlockConditionSerializer<AdjacentBlockCondition> getSerializer() {
        return BlockConditionSerializers.ADJACENT.get();
    }

    public static class Serializer extends BlockConditionSerializer<AdjacentBlockCondition> {

        @Override
        public MapCodec<AdjacentBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, AdjacentBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Adjacent")
                    .setDescription("Checks whether a specified amount of blocks adjacent to the block in question fulfills block conditions.")
                    .add("adjacent_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "The block conditions that need to be fulfilled by adjacent blocks to count towards the check.")
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator being used")
                    .add("compare_to", TYPE_FLOAT, "The value that is being compared against.")
                    .addExampleObject(new AdjacentBlockCondition(new BlockBlockCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("iron_ore"))))), NumberComparator.GREATER_OR_EQUAL, 4));
        }
    }

}