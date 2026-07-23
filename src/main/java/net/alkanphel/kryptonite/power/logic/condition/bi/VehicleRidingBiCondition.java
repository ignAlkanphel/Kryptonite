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

public record VehicleRidingBiCondition() implements BiCondition {

    public static final MapCodec<VehicleRidingBiCondition> CODEC = MapCodec.unit(new VehicleRidingBiCondition());
    public static final StreamCodec<RegistryFriendlyByteBuf, VehicleRidingBiCondition> STREAM_CODEC = StreamCodec.unit(new VehicleRidingBiCondition());

    @Override
    public boolean test(BiConditionContext context) {
        return Objects.equals(context.actor().getVehicle(), context.target());
    }

    @Override
    public BiConditionSerializer<VehicleRidingBiCondition> getSerializer() {
        return BiConditionSerializers.VEHICLE_RIDING.get();
    }

    public static class Serializer extends BiConditionSerializer<VehicleRidingBiCondition> {

        @Override
        public MapCodec<VehicleRidingBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, VehicleRidingBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Vehicle Riding")
                    .setDescription("Checks if the actor entity is currently riding the target entity.")
                    .addExampleObject(new VehicleRidingBiCondition());
        }
    }

}