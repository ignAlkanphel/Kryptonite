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

public record NotDimensionCondition(List<DimensionCondition> conditions) implements DimensionCondition {

    public static final MapCodec<NotDimensionCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DimensionCondition.LIST_CODEC.fieldOf("conditions").forGetter(NotDimensionCondition::conditions)
    ).apply(instance, NotDimensionCondition::new));

    @Override
    public boolean test(DimensionConditionContext context) {
        for (DimensionCondition condition : this.conditions) {
            if (condition.test(context)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public DimensionConditionSerializer<NotDimensionCondition> getSerializer() {
        return DimensionConditionSerializers.NOT.get();
    }

    public static class Serializer extends DimensionConditionSerializer<NotDimensionCondition> {

        @Override
        public MapCodec<NotDimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DimensionCondition, NotDimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("NOT")
                    .setDescription("Allows you to group multiple dimension conditions into one using the NOT logic. None of the given dimension conditions must be true for this one to be true aswell.")
                    .add("conditions", KryptoniteDocumented.TYPE_DIMENSION_CONDITION_LIST, "List of dimension conditions")
                    .addExampleObject(new NotDimensionCondition(Arrays.asList(new HasEnderDragonFightDimensionCondition(), new AttributesDimensionCondition(EnvironmentAttributeMap.builder().set(EnvironmentAttributes.BED_RULE, BedRule.EXPLODES).set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false).build()))));
        }
    }

}