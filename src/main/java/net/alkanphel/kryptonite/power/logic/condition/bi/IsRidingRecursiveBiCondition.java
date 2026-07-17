package net.alkanphel.kryptonite.power.logic.condition.bi;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BiConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.Objects;

public record IsRidingRecursiveBiCondition() implements BiCondition {

    public static final MapCodec<IsRidingRecursiveBiCondition> CODEC = MapCodec.unit(new IsRidingRecursiveBiCondition());
    public static final StreamCodec<RegistryFriendlyByteBuf, IsRidingRecursiveBiCondition> STREAM_CODEC = StreamCodec.unit(new IsRidingRecursiveBiCondition());

    @Override
    public boolean test(BiConditionContext context) {
        Entity vehicle = context.actor().getVehicle();

        while (vehicle != null) {
            if (Objects.equals(vehicle, context.target())) {
                return true;
            }
            else {
                vehicle = vehicle.getVehicle();
            }
        }

        return false;
    }

    @Override
    public BiConditionSerializer<IsRidingRecursiveBiCondition> getSerializer() {
        return BiConditionSerializers.IS_RIDING_RECURSIVE.get();
    }

    public static class Serializer extends BiConditionSerializer<IsRidingRecursiveBiCondition> {

        @Override
        public MapCodec<IsRidingRecursiveBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, IsRidingRecursiveBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Is Riding Recursive")
                    .setDescription("Checks if the actor entity is riding the target entity, regardless of where the target entity is in the riding chain.")
                    .addExampleObject(new IsRidingRecursiveBiCondition());
        }
    }

}