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

public record NotBiCondition(List<BiCondition> conditions) implements BiCondition {

    public static final MapCodec<NotBiCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.compactListCodec(BiCondition.FALSE_TRUE_WRAPPED_CODEC).fieldOf("conditions").forGetter(NotBiCondition::conditions)
    ).apply(instance, NotBiCondition::new));

    @Override
    public boolean test(BiConditionContext context) {
        for (BiCondition condition : this.conditions) {
            if (condition.test(context)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public BiConditionSerializer<?> getSerializer() {
        return BiConditionSerializers.NOT.get();
    }

    public static class Serializer extends BiConditionSerializer<NotBiCondition> {

        @Override
        public MapCodec<NotBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, NotBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("NOT")
                    .setDescription("Allows you to group multiple bi conditions into one using the NOT logic. None of the given bi conditions must be true for this one to be true aswell.")
                    .add("conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "List of bi conditions")
                    .addExampleObject(new NotBiCondition(Arrays.asList(TrueBiCondition.INSTANCE, TrueBiCondition.INSTANCE)));
        }
    }

}