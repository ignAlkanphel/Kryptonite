package net.alkanphel.kryptonite.power.logic.condition.bi;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BiConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Attackable;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.Objects;

public record AttackerBiCondition() implements BiCondition {

    public static final MapCodec<AttackerBiCondition> CODEC = MapCodec.unit(new AttackerBiCondition());
    public static final StreamCodec<RegistryFriendlyByteBuf, AttackerBiCondition> STREAM_CODEC = StreamCodec.unit(new AttackerBiCondition());

    @Override
    public boolean test(BiConditionContext context) {
        return context.target() instanceof Attackable attackable
                && Objects.equals(context.actor(), attackable.getLastAttacker());
    }

    @Override
    public BiConditionSerializer<AttackerBiCondition> getSerializer() {
        return BiConditionSerializers.ATTACKER.get();
    }

    public static class Serializer extends BiConditionSerializer<AttackerBiCondition> {

        @Override
        public MapCodec<AttackerBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, AttackerBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Attacker")
                    .setDescription("Checks if the actor entity has attacked the target entity within the last 5 seconds & still exists.")
                    .addExampleObject(new AttackerBiCondition());
        }
    }

}