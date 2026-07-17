package net.alkanphel.kryptonite.power.logic.condition.bi;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BiConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.Objects;

public record EqualBiCondition() implements BiCondition {

    public static final MapCodec<EqualBiCondition> CODEC = MapCodec.unit(new EqualBiCondition());
    public static final StreamCodec<RegistryFriendlyByteBuf, EqualBiCondition> STREAM_CODEC = StreamCodec.unit(new EqualBiCondition());

    @Override
    public boolean test(BiConditionContext context) {
        return Objects.equals(context.actor(), context.target());
    }

    @Override
    public BiConditionSerializer<EqualBiCondition> getSerializer() {
        return BiConditionSerializers.EQUAL.get();
    }

    public static class Serializer extends BiConditionSerializer<EqualBiCondition> {

        @Override
        public MapCodec<EqualBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, EqualBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Equal")
                    .setDescription("Checks if the actor entity is the target entity.")
                    .addExampleObject(new EqualBiCondition());
        }
    }

}