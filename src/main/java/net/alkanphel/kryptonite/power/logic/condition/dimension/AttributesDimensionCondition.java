package net.alkanphel.kryptonite.power.logic.condition.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionCondition;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.DimensionConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.attribute.*;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public record AttributesDimensionCondition(@Nullable EnvironmentAttributeMap attributes) implements DimensionCondition {

    public static final MapCodec<AttributesDimensionCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EnvironmentAttributeMap.CODEC.optionalFieldOf("attributes").forGetter(c -> Optional.ofNullable(c.attributes()))
    ).apply(instance, opt -> new AttributesDimensionCondition(opt.orElse(null))));

    @Override
    public boolean test(DimensionConditionContext context) {
        if (attributes == null) return true;

        var level = context.level();
        var entity = context.entity();
        if (level == null || entity == null) return false;

        EnvironmentAttributeSystem system = level.environmentAttributes();
        Vec3 pos = entity.position();

        for (EnvironmentAttribute attribute : attributes.keySet()) {
            var expectedEntry = attributes.get(attribute);
            if (expectedEntry == null) return false;

            Object expectedValue = expectedEntry.applyModifier(attribute.defaultValue());
            Object actualValue = attribute.isPositional()
                    ? system.getValue(attribute, pos)
                    : system.getDimensionValue(attribute);

            if (!Objects.equals(expectedValue, actualValue)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public DimensionConditionSerializer<?> getSerializer() {
        return DimensionConditionSerializers.ATTRIBUTES.get();
    }

    public static class Serializer extends DimensionConditionSerializer<AttributesDimensionCondition> {

        @Override
        public MapCodec<AttributesDimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DimensionCondition, AttributesDimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Attributes")
                    .setDescription("Checks environment attributes in the current dimension.")
                    .addOptional("attributes", KryptoniteDocumented.TYPE_JSON_OBJECT, "Environment attributes to match.")
                    .addExampleObject(new AttributesDimensionCondition(EnvironmentAttributeMap.builder().set(EnvironmentAttributes.BED_RULE, BedRule.EXPLODES).set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, true).build()));
        }
    }

}