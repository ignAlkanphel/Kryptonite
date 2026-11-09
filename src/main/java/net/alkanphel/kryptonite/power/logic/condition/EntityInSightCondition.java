package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.util.KryptoniteCodecs;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public record EntityInSightCondition(ClipContext.Block shapeType, ClipContext.Fluid fluidHandling) implements Condition {

    public static final MapCodec<EntityInSightCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KryptoniteCodecs.CLIP_CONTEXT_BLOCK_CODEC.optionalFieldOf("shape_type", ClipContext.Block.VISUAL).forGetter(EntityInSightCondition::shapeType),
            KryptoniteCodecs.CLIP_CONTEXT_FLUID_CODEC.optionalFieldOf("fluid_handling", ClipContext.Fluid.NONE).forGetter(EntityInSightCondition::fluidHandling)
    ).apply(instance, EntityInSightCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityInSightCondition> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(ClipContext.Block.class), EntityInSightCondition::shapeType,
            NeoForgeStreamCodecs.enumCodec(ClipContext.Fluid.class), EntityInSightCondition::fluidHandling,
            EntityInSightCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        Entity source = context.getSource();
        Entity target = context.getTarget();

        if (source == null || target == null) return false;
        if (source.level() != target.level()) return false;

        Vec3 sourceEyePos = source.getEyePosition();
        Vec3 targetEyePos = target.getEyePosition();

        ClipContext clipContext = new ClipContext(sourceEyePos, targetEyePos, shapeType, fluidHandling, source);
        return source.level().clip(clipContext).getType() == HitResult.Type.MISS;
    }

    @Override
    public ConditionSerializer<?> getSerializer() {
        return KryptoniteConditionSerializers.ENTITY_IN_SIGHT.get();
    }

    public static class Serializer extends ConditionSerializer<EntityInSightCondition> {

        @Override
        public MapCodec<EntityInSightCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, EntityInSightCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Entity In Sight")
                    .setDescription("Checks if the straight path from the source entity's eyes to the target entity's eyes is unobstructed.")
                    .addOptional("shape_type", KryptoniteDocumented.TYPE_CLIP_CONTEXT_BLOCK, "Determines how the raycast will handle blocks.", ClipContext.Block.VISUAL)
                    .addOptional("fluid_handling", KryptoniteDocumented.TYPE_CLIP_CONTEXT_FLUID, "Determines how the raycast will handle fluids.", ClipContext.Fluid.NONE)
                    .addExampleObject(new EntityInSightCondition(ClipContext.Block.VISUAL, ClipContext.Fluid.NONE));
        }
    }

}