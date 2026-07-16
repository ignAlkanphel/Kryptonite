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

public record TargetConditionBiCondition(Condition condition) implements BiCondition {

    public static final MapCodec<TargetConditionBiCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Condition.CODEC.fieldOf("conditions").forGetter(TargetConditionBiCondition::condition)
    ).apply(instance, TargetConditionBiCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TargetConditionBiCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Condition.CODEC), TargetConditionBiCondition::condition,
            TargetConditionBiCondition::new
    );

    @Override
    public boolean test(BiConditionContext context) {
        if (context.target() == null) return false;
        return condition.test(DataContext.forEntity(context.target()));
    }

    @Override
    public BiConditionSerializer<TargetConditionBiCondition> getSerializer() {
        return BiConditionSerializers.TARGET_CONDITION.get();
    }

    public static class Serializer extends BiConditionSerializer<TargetConditionBiCondition> {

        @Override
        public MapCodec<TargetConditionBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, TargetConditionBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Target Condition")
                    .setDescription("Checks if the target entity fulfills the specified conditions.")
                    .add("conditions", TYPE_CONDITION_LIST, "The conditions to check for on the target entity.")
                    .addExampleObject(new TargetConditionBiCondition(new net.threetag.palladium.logic.condition.CrouchingCondition()));
        }
    }

}