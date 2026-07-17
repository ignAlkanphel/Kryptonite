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
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Targeting;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.Objects;

public record AttackTargetBiCondition() implements BiCondition {

    public static final MapCodec<AttackTargetBiCondition> CODEC = MapCodec.unit(new AttackTargetBiCondition());
    public static final StreamCodec<RegistryFriendlyByteBuf, AttackTargetBiCondition> STREAM_CODEC = StreamCodec.unit(new AttackTargetBiCondition());

    @Override
    public boolean test(BiConditionContext context) {
        Entity actor = context.actor();
        Entity target = context.target();

        return (actor instanceof Targeting targeterActor && Objects.equals(target, targeterActor.getTarget())) ||
               (actor instanceof NeutralMob angerableActor && Objects.equals(target, angerableActor.getTarget()));
    }

    @Override
    public BiConditionSerializer<AttackTargetBiCondition> getSerializer() {
        return BiConditionSerializers.ATTACK_TARGET.get();
    }

    public static class Serializer extends BiConditionSerializer<AttackTargetBiCondition> {

        @Override
        public MapCodec<AttackTargetBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, AttackTargetBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Attack Target")
                    .setDescription("Checks if the actor entity is currently aggressive to the target entity.")
                    .addExampleObject(new AttackTargetBiCondition());
        }
    }

}