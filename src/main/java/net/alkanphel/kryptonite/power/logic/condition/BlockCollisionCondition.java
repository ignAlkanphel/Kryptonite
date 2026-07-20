package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

import java.util.List;
import java.util.Objects;

public record BlockCollisionCondition(Value offsetX, Value offsetY, Value offsetZ, List<BlockCondition> blockConditions) implements Condition {

    public static final MapCodec<BlockCollisionCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.optionalFieldOf("offset_x", new StaticValue(0)).forGetter(BlockCollisionCondition::offsetX),
            Value.CODEC.optionalFieldOf("offset_y", new StaticValue(0)).forGetter(BlockCollisionCondition::offsetY),
            Value.CODEC.optionalFieldOf("offset_z", new StaticValue(0)).forGetter(BlockCollisionCondition::offsetZ),
            BlockCondition.LIST_CODEC.optionalFieldOf("block_conditions", List.of()).forGetter(BlockCollisionCondition::blockConditions)
    ).apply(instance, BlockCollisionCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockCollisionCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), BlockCollisionCondition::offsetX,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), BlockCollisionCondition::offsetY,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), BlockCollisionCondition::offsetZ,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(BlockCondition.LIST_CODEC), BlockCollisionCondition::blockConditions,
            BlockCollisionCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        Level level = context.getLevel();
        if (level == null) return false;

        AABB bb = Objects.requireNonNull(context.getEntity()).getBoundingBox().move(offsetX.getAsFloat(context), offsetY.getAsFloat(context), offsetZ.getAsFloat(context)).deflate(0.001);
        BlockPos min = BlockPos.containing(bb.minX, bb.minY, bb.minZ);
        BlockPos max = BlockPos.containing(bb.maxX, bb.maxY, bb.maxZ);

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            if (state.getCollisionShape(level, pos).isEmpty()) continue;
            if (blockConditions.isEmpty() || BlockCondition.checkConditions(blockConditions, level, pos)) return true;
        }

        return false;
    }

    @Override
    public ConditionSerializer<BlockCollisionCondition> getSerializer() {
        return KryptoniteConditionSerializers.BLOCK_COLLISION.get();
    }

    public static class Serializer extends ConditionSerializer<BlockCollisionCondition> {

        @Override
        public MapCodec<BlockCollisionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, BlockCollisionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Block Collision")
                    .setDescription("Checks whether the bounding box of the entity collides with a block.")
                    .addOptional("offset_x", TYPE_FLOAT, "The bounding box size should the box be offset in the X direction (e.g.: 0 = no offset, 1 = offset of exact width, 2 = offset of twice the width of the bounding box)\n")
                    .addOptional("offset_y", TYPE_FLOAT, "The bounding box size should the box be offset in the Y direction (e.g.: 0 = no offset, 1 = offset of exact height, 2 = offset of twice the height of the bounding box)\n")
                    .addOptional("offset_z", TYPE_FLOAT, "The beounding box size offset in the Z direction (e.g.: 0 = no offset, 1 = offset of exact depth, 2 = offset of twice the depth of the bounding box)\n")
                    .addOptional("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, these conditions must be fulfilled for the colliding block.")
                    .addExampleObject(new BlockCollisionCondition(new StaticValue(0.1), new StaticValue(0), new StaticValue(0.1), List.of()));
        }
    }

}