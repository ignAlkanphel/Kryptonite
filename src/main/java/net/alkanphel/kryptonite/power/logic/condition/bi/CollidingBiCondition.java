package net.alkanphel.kryptonite.power.logic.condition.bi;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BiConditionContext;
import net.alkanphel.kryptonite.util.KryptoniteCodecs;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record CollidingBiCondition(Vec3 offset) implements BiCondition {

    public static final MapCodec<CollidingBiCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KryptoniteCodecs.VEC3_OBJECT.optionalFieldOf("offset", Vec3.ZERO).forGetter(CollidingBiCondition::offset)
    ).apply(instance, CollidingBiCondition::new));

    @Override
    public boolean test(BiConditionContext context) {
        var actor = context.actor();
        var target = context.target();
        if (actor == null || target == null) return false;

        return actor.getBoundingBox().move(this.offset).intersects(target.getBoundingBox());
    }

    @Override
    public BiConditionSerializer<?> getSerializer() {
        return BiConditionSerializers.COLLIDING.get();
    }

    public static class Serializer extends BiConditionSerializer<CollidingBiCondition> {

        @Override
        public MapCodec<CollidingBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, CollidingBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Colliding")
                    .setDescription("Checks if the actor entity is colliding with the target entity.")
                    .addOptional("offset", TYPE_VECTOR3, "If specified, this offset will be applied to the actor entity's bounding box.")
                    .addExampleObject(new CollidingBiCondition(new Vec3(0.5, 0.0, 0.5)))
                    .addExampleObject(new CollidingBiCondition(new Vec3(-0.5, 0.0, -0.5)));
        }
    }

}