package net.alkanphel.kryptonite.power.logic.condition.dimension.meta;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.dimension.AttributesDimensionCondition;
import net.alkanphel.kryptonite.power.logic.condition.dimension.HasEnderDragonFightDimensionCondition;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionCondition;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.DimensionConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.Arrays;
import java.util.List;

public record OrDimensionCondition(List<DimensionCondition> conditions) implements DimensionCondition {

    public static final MapCodec<OrDimensionCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DimensionCondition.LIST_CODEC.fieldOf("conditions").forGetter(OrDimensionCondition::conditions)
    ).apply(instance, OrDimensionCondition::new));

    @Override
    public boolean test(DimensionConditionContext context) {
        for (DimensionCondition condition : this.conditions) {
            if (condition.test(context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DimensionConditionSerializer<OrDimensionCondition> getSerializer() {
        return DimensionConditionSerializers.OR.get();
    }

    public static class Serializer extends DimensionConditionSerializer<OrDimensionCondition> {

        @Override
        public MapCodec<OrDimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DimensionCondition, OrDimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("OR")
                    .setDescription("Allows you to group multiple dimension conditions into one using the OR logic. At least one of the given dimension conditions must be true for this one to be true aswell.")
                    .add("conditions", KryptoniteDocumented.TYPE_DIMENSION_CONDITION_LIST, "List of dimension conditions")
                    .addExampleObject(new OrDimensionCondition(Arrays.asList(new HasEnderDragonFightDimensionCondition(), new AttributesDimensionCondition(EnvironmentAttributeMap.builder().set(EnvironmentAttributes.BED_RULE, BedRule.EXPLODES).set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false).build()))));
        }
    }

}