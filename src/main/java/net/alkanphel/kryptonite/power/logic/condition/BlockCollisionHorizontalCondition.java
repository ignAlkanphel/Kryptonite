package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public class BlockCollisionHorizontalCondition implements Condition {

    public static final BlockCollisionHorizontalCondition INSTANCE = new BlockCollisionHorizontalCondition();

    public static final MapCodec<BlockCollisionHorizontalCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockCollisionHorizontalCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        return context.getEntity().horizontalCollision;
    }

    @Override
    public ConditionSerializer<BlockCollisionHorizontalCondition> getSerializer() {
        return KryptoniteConditionSerializers.BLOCK_COLLISION_HORIZONTAL.get();
    }

    public static class Serializer extends ConditionSerializer<BlockCollisionHorizontalCondition> {

        @Override
        public MapCodec<BlockCollisionHorizontalCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, BlockCollisionHorizontalCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Block Collision Horizontal")
                    .setDescription("Checks if the entity is colliding horizontally against a block.")
                    .addExampleObject(INSTANCE);
        }
    }

}