package net.alkanphel.kryptonite.power.logic.condition.bi.meta;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BiConditionContext;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.Arrays;
import java.util.List;

public record AndBiCondition(List<BiCondition> conditions) implements BiCondition {

    public static final MapCodec<AndBiCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiCondition.FALSE_TRUE_WRAPPED_CODEC.listOf().fieldOf("conditions").forGetter(AndBiCondition::conditions)
    ).apply(instance, AndBiCondition::new));

    @Override
    public boolean test(BiConditionContext context) {
        for (BiCondition condition : this.conditions) {
            if (!condition.test(context)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public BiConditionSerializer<?> getSerializer() {
        return BiConditionSerializers.AND.get();
    }

    public static class Serializer extends BiConditionSerializer<AndBiCondition> {

        @Override
        public MapCodec<AndBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, AndBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("AND")
                    .setDescription("Allows you to group multiple bi conditions into one using the AND logic. All of the given bi conditions must be true for this one to be true aswell.")
                    .add("conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "List of bi conditions")
                    .addExampleObject(new AndBiCondition(Arrays.asList(TrueBiCondition.INSTANCE, TrueBiCondition.INSTANCE)));
        }
    }

}