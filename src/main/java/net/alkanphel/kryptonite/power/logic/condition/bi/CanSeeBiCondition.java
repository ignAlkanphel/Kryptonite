package net.alkanphel.kryptonite.power.logic.condition.bi;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BiConditionContext;
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

public record CanSeeBiCondition(ClipContext.Block shapeType, ClipContext.Fluid fluidHandling) implements BiCondition {

    public static final MapCodec<CanSeeBiCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KryptoniteCodecs.CLIP_CONTEXT_BLOCK_CODEC.optionalFieldOf("shape_type", ClipContext.Block.VISUAL).forGetter(CanSeeBiCondition::shapeType),
            KryptoniteCodecs.CLIP_CONTEXT_FLUID_CODEC.optionalFieldOf("fluid_handling", ClipContext.Fluid.NONE).forGetter(CanSeeBiCondition::fluidHandling)
    ).apply(instance, CanSeeBiCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CanSeeBiCondition> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(ClipContext.Block.class), CanSeeBiCondition::shapeType,
            NeoForgeStreamCodecs.enumCodec(ClipContext.Fluid.class), CanSeeBiCondition::fluidHandling,
            CanSeeBiCondition::new
    );

    @Override
    public boolean test(BiConditionContext context) {
        Entity actor = context.actor();
        Entity target = context.target();

        if (actor == null || target == null) return false;
        if (actor.level() != target.level()) return false;

        Vec3 actorEyePos = actor.getEyePosition();
        Vec3 targetEyePos = target.getEyePosition();

        ClipContext clipContext = new ClipContext(actorEyePos, targetEyePos, shapeType, fluidHandling, actor);
        return actor.level().clip(clipContext).getType() == HitResult.Type.MISS;
    }

    @Override
    public BiConditionSerializer<CanSeeBiCondition> getSerializer() {
        return BiConditionSerializers.CAN_SEE.get();
    }

    public static class Serializer extends BiConditionSerializer<CanSeeBiCondition> {

        @Override
        public MapCodec<CanSeeBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, CanSeeBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Can See")
                    .setDescription("Checks if the straight path from the actor entity's eyes to the target entity's eyes is unobstructed.")
                    .addOptional("shape_type", KryptoniteDocumented.TYPE_CLIP_CONTEXT_BLOCK, "Determines how the raycast will handle blocks.", ClipContext.Block.VISUAL)
                    .addOptional("fluid_handling", KryptoniteDocumented.TYPE_CLIP_CONTEXT_FLUID, "Determines how the raycast will handle fluids.", ClipContext.Fluid.NONE)
                    .addExampleObject(new CanSeeBiCondition(ClipContext.Block.VISUAL, ClipContext.Fluid.NONE));
        }
    }

}