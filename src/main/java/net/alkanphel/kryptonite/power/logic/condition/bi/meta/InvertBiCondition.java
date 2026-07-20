package net.alkanphel.kryptonite.power.logic.condition.bi.meta;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BiConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record InvertBiCondition(BiCondition biEntityCondition) implements BiCondition {

    public static final MapCodec<InvertBiCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiCondition.CODEC.fieldOf("conditions").forGetter(InvertBiCondition::biEntityCondition)
    ).apply(instance, InvertBiCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InvertBiCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(BiCondition.CODEC), InvertBiCondition::biEntityCondition,
            InvertBiCondition::new
    );

    @Override
    public boolean test(BiConditionContext context) {
        return biEntityCondition.test(context.target(), context.actor());
    }

    @Override
    public BiConditionSerializer<InvertBiCondition> getSerializer() {
        return BiConditionSerializers.INVERT.get();
    }

    public static class Serializer extends BiConditionSerializer<InvertBiCondition> {

        @Override
        public MapCodec<InvertBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, InvertBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Invert")
                    .setDescription("Swaps contexts of target & actor entity.")
                    .add("conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "The bi conditions to check with the context swapped.")
                    .addExampleObject(new InvertBiCondition(new TrueBiCondition()));
        }
    }

}