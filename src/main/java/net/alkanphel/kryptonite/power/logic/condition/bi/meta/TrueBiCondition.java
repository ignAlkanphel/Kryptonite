package net.alkanphel.kryptonite.power.logic.condition.bi.meta;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BiConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public final class TrueBiCondition implements BiCondition {

    public static final TrueBiCondition INSTANCE = new TrueBiCondition();

    public static final MapCodec<TrueBiCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, TrueBiCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean test(BiConditionContext context) {
        return true;
    }

    @Override
    public BiConditionSerializer<?> getSerializer() {
        return BiConditionSerializers.TRUE.get();
    }

    public static class Serializer extends BiConditionSerializer<TrueBiCondition> {

        @Override
        public MapCodec<TrueBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, TrueBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("TRUE")
                    .setDescription("It's just true. That's it.")
                    .addExampleObject(new TrueBiCondition());
        }
    }

}