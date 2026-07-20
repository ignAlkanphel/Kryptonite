package net.alkanphel.kryptonite.power.logic.condition.bi.meta;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.bi.OwnerBiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BiConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record UndirectedBiCondition(BiCondition biEntityCondition) implements BiCondition {

    public static final MapCodec<UndirectedBiCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiCondition.CODEC.fieldOf("conditions").forGetter(UndirectedBiCondition::biEntityCondition)
    ).apply(instance, UndirectedBiCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, UndirectedBiCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(BiCondition.CODEC), UndirectedBiCondition::biEntityCondition,
            UndirectedBiCondition::new
    );

    @Override
    public boolean test(BiConditionContext context) {
        return biEntityCondition.test(context) || biEntityCondition.test(context.target(), context.actor());
    }

    @Override
    public BiConditionSerializer<UndirectedBiCondition> getSerializer() {
        return BiConditionSerializers.UNDIRECTED.get();
    }

    public static class Serializer extends BiConditionSerializer<UndirectedBiCondition> {

        @Override
        public MapCodec<UndirectedBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, UndirectedBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Undirected")
                    .setDescription("Checks if these conditions are true before or after swapping the actor & target context. The example listed: WITHOUT \"undirected\", it's true if A owns B or B owns A. Whereas without \"undirected\", it would only be true if A owns B.")
                    .add("conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "The bi condition type to check for.")
                    .addExampleObject(new UndirectedBiCondition(new OwnerBiCondition()));
        }
    }

}