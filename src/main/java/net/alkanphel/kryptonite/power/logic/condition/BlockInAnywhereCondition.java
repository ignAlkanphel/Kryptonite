package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.block.BlockBlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.phys.AABB;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;

import java.util.List;

public record BlockInAnywhereCondition(List<BlockCondition> blockConditions, NumberComparator comparator, Value compareTo) implements Condition {

    public static final MapCodec<BlockInAnywhereCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockCondition.LIST_CODEC.optionalFieldOf("block_conditions", List.of()).forGetter(BlockInAnywhereCondition::blockConditions),
            NumberComparator.CODEC.optionalFieldOf("comparator", NumberComparator.GREATER_OR_EQUAL).forGetter(BlockInAnywhereCondition::comparator),
            Value.CODEC.optionalFieldOf("compare_to", new StaticValue(1)).forGetter(BlockInAnywhereCondition::compareTo)
    ).apply(instance, BlockInAnywhereCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockInAnywhereCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(BlockCondition.LIST_CODEC), BlockInAnywhereCondition::blockConditions,
            NumberComparator.STREAM_CODEC, BlockInAnywhereCondition::comparator,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), BlockInAnywhereCondition::compareTo,
            BlockInAnywhereCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        AABB boundingBox = entity.getBoundingBox();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        BlockPos minPos = BlockPos.containing(boundingBox.minX + 0.001D, boundingBox.minY + 0.001D, boundingBox.minZ + 0.001D);
        BlockPos maxPos = BlockPos.containing(boundingBox.maxX - 0.001D, boundingBox.maxY - 0.001D, boundingBox.maxZ - 0.001D);

        int matches = 0;
        int compareTo = this.compareTo.getAsInt(context);

        int threshold = switch (comparator) {
            case EQUALS, LESS_OR_EQUAL, GREATER_THAN -> compareTo + 1;
            case LESS_THAN, GREATER_OR_EQUAL -> compareTo;
            default -> -1;
        };

        for (int x = minPos.getX(); x <= maxPos.getX() && matches < threshold; x++) {
            for (int y = minPos.getY(); y <= maxPos.getY() && matches < threshold; y++) {
                for (int z = minPos.getZ(); z <= maxPos.getZ() && matches < threshold; z++) {
                    mutablePos.set(x, y, z);

                    if (blockConditions.isEmpty() || BlockCondition.checkConditions(blockConditions, entity.level(), mutablePos)) {
                        matches++;
                    }
                }
            }
        }

        return comparator.compare(matches, compareTo);
    }

    @Override
    public ConditionSerializer<BlockInAnywhereCondition> getSerializer() {
        return KryptoniteConditionSerializers.BLOCK_IN_ANYWHERE.get();
    }

    public static class Serializer extends ConditionSerializer<BlockInAnywhereCondition> {

        @Override
        public MapCodec<BlockInAnywhereCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, BlockInAnywhereCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Block In Anywhere")
                    .setDescription("Checks how many blocks are overlapping with the entity's eyes or feet.")
                    .addOptional("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, these conditions must be fulfilled for the overlapping blocks.")
                    .addOptional("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator being used", NumberComparator.GREATER_OR_EQUAL)
                    .addOptional("compare_to", TYPE_VALUE, "The value that is being compared against", new StaticValue(1))
                    .addExampleObject(new BlockInAnywhereCondition(List.of(new BlockBlockCondition(provider.lookupOrThrow(Registries.BLOCK).getOrThrow(BlockTags.FLOWERS))), NumberComparator.GREATER_THAN, new StaticValue(1)));
        }
    }

}