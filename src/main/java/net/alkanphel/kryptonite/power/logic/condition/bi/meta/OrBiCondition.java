package net.alkanphel.kryptonite.power.logic.condition.bi.meta;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BiConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ExtraCodecs;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public record OrBiCondition(List<BiCondition> biConditions) implements BiCondition {

    public static final MapCodec<OrBiCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.compactListCodec(BiCondition.FALSE_TRUE_WRAPPED_CODEC).optionalFieldOf("bi_conditions").forGetter(c -> Optional.of(c.biConditions())),
            ExtraCodecs.compactListCodec(BiCondition.FALSE_TRUE_WRAPPED_CODEC).optionalFieldOf("bientity_conditions").forGetter(c -> Optional.empty()),
            ExtraCodecs.compactListCodec(BiCondition.FALSE_TRUE_WRAPPED_CODEC).optionalFieldOf("conditions").forGetter(c -> Optional.empty())
    ).apply(instance, (biConditions, biEntityConditions, conditions) ->
            new OrBiCondition(biConditions.orElseGet(() -> biEntityConditions.orElseGet(() -> conditions.orElse(List.of()))))
    ));

    @Override
    public boolean test(BiConditionContext context) {
        for (BiCondition biCondition : this.biConditions) {
            if (biCondition.test(context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BiConditionSerializer<?> getSerializer() {
        return BiConditionSerializers.OR.get();
    }

    public static class Serializer extends BiConditionSerializer<OrBiCondition> {

        @Override
        public MapCodec<OrBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, OrBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("OR")
                    .setDescription("Allows you to group multiple bi conditions into one using the OR logic. At least one of the given bi conditions must be true for this one to be true aswell. The namespace alias \"palladium:or\" is supported.")
                    .add("conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "List of bi conditions. This field supports aliases: \"bi_conditions\", \"bienity_conditions\", & \"conditions\"")
                    .addExampleObject(new OrBiCondition(Arrays.asList(TrueBiCondition.INSTANCE, TrueBiCondition.INSTANCE)));
        }
    }

}