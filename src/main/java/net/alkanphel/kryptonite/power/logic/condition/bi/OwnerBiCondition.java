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
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.Objects;

public record OwnerBiCondition() implements BiCondition {

    public static final MapCodec<OwnerBiCondition> CODEC = MapCodec.unit(new OwnerBiCondition());
    public static final StreamCodec<RegistryFriendlyByteBuf, OwnerBiCondition> STREAM_CODEC = StreamCodec.unit(new OwnerBiCondition());

    @Override
    public boolean test(BiConditionContext context) {
        Entity actor = context.actor();
        Entity target = context.target();

        return (target instanceof TamableAnimal tameableTarget && Objects.equals(actor, tameableTarget.getOwner())) ||
               (target instanceof OwnableEntity ownableTarget && Objects.equals(actor, ownableTarget.getOwner()));
    }

    @Override
    public BiConditionSerializer<OwnerBiCondition> getSerializer() {
        return BiConditionSerializers.OWNER.get();
    }

    public static class Serializer extends BiConditionSerializer<OwnerBiCondition> {

        @Override
        public MapCodec<OwnerBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, OwnerBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Owner")
                    .setDescription("Checks whether the actor entity is the owner of the tamable target entity.")
                    .addExampleObject(new OwnerBiCondition());
        }
    }

}