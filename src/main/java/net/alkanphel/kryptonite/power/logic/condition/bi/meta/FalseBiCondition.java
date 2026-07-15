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

public final class FalseBiCondition implements BiCondition {

    public static final FalseBiCondition INSTANCE = new FalseBiCondition();

    public static final MapCodec<FalseBiCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, FalseBiCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean test(BiConditionContext context) {
        return false;
    }

    @Override
    public BiConditionSerializer<?> getSerializer() {
        return BiConditionSerializers.FALSE.get();
    }

    public static class Serializer extends BiConditionSerializer<FalseBiCondition> {

        @Override
        public MapCodec<FalseBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, FalseBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("FALSE")
                    .setDescription("It's just false. That's it.")
                    .addExampleObject(new FalseBiCondition());
        }
    }

}