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
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;

public record CollisionBiCondition(KryptoniteCodecs.Vec3Value offset) implements BiCondition {

    public static final MapCodec<CollisionBiCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KryptoniteCodecs.Vec3Value.CODEC.optionalFieldOf("offset", KryptoniteCodecs.Vec3Value.ZERO).forGetter(CollisionBiCondition::offset)
    ).apply(instance, CollisionBiCondition::new));

    @Override
    public boolean test(BiConditionContext context) {
        var actor = context.actor();
        var target = context.target();
        if (actor == null || target == null) return false;

        var dataContext = DataContext.forEntity(actor);

        return actor.getBoundingBox().move(this.offset.get(dataContext)).intersects(target.getBoundingBox());
    }

    @Override
    public BiConditionSerializer<?> getSerializer() {
        return BiConditionSerializers.COLLISION.get();
    }

    public static class Serializer extends BiConditionSerializer<CollisionBiCondition> {

        @Override
        public MapCodec<CollisionBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, CollisionBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Collision")
                    .setDescription("Checks if the actor entity is colliding with the target entity.")
                    .addOptional("offset", KryptoniteDocumented.TYPE_VECTOR3_VALUE, "If specified, this offset will be applied to the actor entity's bounding box.", Vec3.ZERO)
                    .addExampleObject(new CollisionBiCondition(new KryptoniteCodecs.Vec3Value(new StaticValue(0.5D), new StaticValue(0.0D), new StaticValue(0.5D))))
                    .addExampleObject(new CollisionBiCondition(new KryptoniteCodecs.Vec3Value(new StaticValue(-0.5D), new StaticValue(0.0D), new StaticValue(-0.5D))));
        }
    }

}