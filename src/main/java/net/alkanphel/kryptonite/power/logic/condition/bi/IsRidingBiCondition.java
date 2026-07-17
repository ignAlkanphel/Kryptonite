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

public record IsRidingBiCondition() implements BiCondition {

    public static final MapCodec<IsRidingBiCondition> CODEC = MapCodec.unit(new IsRidingBiCondition());
    public static final StreamCodec<RegistryFriendlyByteBuf, IsRidingBiCondition> STREAM_CODEC = StreamCodec.unit(new IsRidingBiCondition());

    @Override
    public boolean test(BiConditionContext context) {
        return Objects.equals(context.actor().getVehicle(), context.target());
    }

    @Override
    public BiConditionSerializer<IsRidingBiCondition> getSerializer() {
        return BiConditionSerializers.IS_RIDING.get();
    }

    public static class Serializer extends BiConditionSerializer<IsRidingBiCondition> {

        @Override
        public MapCodec<IsRidingBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, IsRidingBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Is Riding")
                    .setDescription("Checks if the actor entity is currently riding the target entity.")
                    .addExampleObject(new IsRidingBiCondition());
        }
    }

}