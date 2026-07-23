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

public record VehicleRidingRootBiCondition() implements BiCondition {

    public static final MapCodec<VehicleRidingRootBiCondition> CODEC = MapCodec.unit(new VehicleRidingRootBiCondition());
    public static final StreamCodec<RegistryFriendlyByteBuf, VehicleRidingRootBiCondition> STREAM_CODEC = StreamCodec.unit(new VehicleRidingRootBiCondition());

    @Override
    public boolean test(BiConditionContext context) {
        return Objects.equals(context.actor().getRootVehicle(), context.target());
    }

    @Override
    public BiConditionSerializer<VehicleRidingRootBiCondition> getSerializer() {
        return BiConditionSerializers.VEHICLE_RIDING_ROOT.get();
    }

    public static class Serializer extends BiConditionSerializer<VehicleRidingRootBiCondition> {

        @Override
        public MapCodec<VehicleRidingRootBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, VehicleRidingRootBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Vehicle Riding Root")
                    .setDescription("Checks if the actor entity is riding the target entity from the very end of the riding chain.")
                    .addExampleObject(new VehicleRidingRootBiCondition());
        }
    }

}