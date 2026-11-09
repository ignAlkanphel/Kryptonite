package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.util.KryptoniteCodecs;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;

public record EntityCollisionCondition(KryptoniteCodecs.Vec3Value offset) implements Condition {

    public static final MapCodec<EntityCollisionCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KryptoniteCodecs.Vec3Value.CODEC.optionalFieldOf("offset", KryptoniteCodecs.Vec3Value.ZERO).forGetter(EntityCollisionCondition::offset)
    ).apply(instance, EntityCollisionCondition::new));

    @Override
    public boolean test(DataContext context) {
        var source = context.getSource();
        var target = context.getTarget();

        if (source == null || target == null) return false;

        var sourceContext = DataContext.forEntity(source);

        return source.getBoundingBox().move(this.offset.get(sourceContext)).intersects(target.getBoundingBox());
    }

    @Override
    public ConditionSerializer<EntityCollisionCondition> getSerializer() {
        return KryptoniteConditionSerializers.ENTITY_COLLISION.get();
    }

    public static class Serializer extends ConditionSerializer<EntityCollisionCondition> {

        @Override
        public MapCodec<EntityCollisionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, EntityCollisionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Entity Collision")
                    .setDescription("Checks if the source entity's bounding box intersects the target entity's bounding box.")
                    .addOptional("offset", KryptoniteDocumented.TYPE_VECTOR3_VALUE, "Offset applied to the source entity's bounding box.", Vec3.ZERO)
                    .addExampleObject(new EntityCollisionCondition(new KryptoniteCodecs.Vec3Value(new StaticValue(0.5D), new StaticValue(0.0D), new StaticValue(0.5D))))
                    .addExampleObject(new EntityCollisionCondition(new KryptoniteCodecs.Vec3Value(new StaticValue(-0.5D), new StaticValue(0.0D), new StaticValue(-0.5D))));
        }
    }

}