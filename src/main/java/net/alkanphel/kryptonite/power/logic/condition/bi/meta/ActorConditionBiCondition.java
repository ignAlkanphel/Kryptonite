package net.alkanphel.kryptonite.power.logic.condition.bi.meta;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BiConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.context.DataContext;

public record ActorConditionBiCondition(Condition condition) implements BiCondition {

    public static final MapCodec<ActorConditionBiCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Condition.CODEC.fieldOf("conditions").forGetter(ActorConditionBiCondition::condition)
    ).apply(instance, ActorConditionBiCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ActorConditionBiCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Condition.CODEC), ActorConditionBiCondition::condition,
            ActorConditionBiCondition::new
    );

    @Override
    public boolean test(BiConditionContext context) {
        if (context.actor() == null) return false;
        return condition.test(DataContext.forEntity(context.actor()));
    }

    @Override
    public BiConditionSerializer<ActorConditionBiCondition> getSerializer() {
        return BiConditionSerializers.ACTOR_CONDITION.get();
    }

    public static class Serializer extends BiConditionSerializer<ActorConditionBiCondition> {

        @Override
        public MapCodec<ActorConditionBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, ActorConditionBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Actor Condition")
                    .setDescription("Checks if the actor entity fulfills the specified conditions.")
                    .add("conditions", TYPE_CONDITION_LIST, "Conditions to check for on the actor entity.")
                    .addExampleObject(new ActorConditionBiCondition(new net.threetag.palladium.logic.condition.CrouchingCondition()));
        }
    }

}