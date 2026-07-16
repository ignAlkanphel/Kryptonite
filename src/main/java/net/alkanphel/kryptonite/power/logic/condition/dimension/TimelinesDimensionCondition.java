package net.alkanphel.kryptonite.power.logic.condition.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionCondition;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.DimensionConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TimelineTags;
import net.minecraft.world.timeline.Timeline;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record TimelinesDimensionCondition(HolderSet<Timeline> timelines) implements DimensionCondition {

    public static final MapCodec<TimelinesDimensionCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.TIMELINE).fieldOf("timelines").forGetter(TimelinesDimensionCondition::timelines)
    ).apply(instance, TimelinesDimensionCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TimelinesDimensionCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderSet(Registries.TIMELINE), TimelinesDimensionCondition::timelines,
            TimelinesDimensionCondition::new
    );

    @Override
    public boolean test(DimensionConditionContext context) {
        if (context.level() == null) return false;

        HolderSet<Timeline> dimensionTimelines = context.dimensionType().timelines();
        if (dimensionTimelines instanceof HolderSet.Named<Timeline> dimTag && timelines instanceof HolderSet.Named<Timeline> conditionTag) {
            return dimTag.key().equals(conditionTag.key());
        }

        return false;
    }

    @Override
    public DimensionConditionSerializer<TimelinesDimensionCondition> getSerializer() {
        return DimensionConditionSerializers.TIMELINES.get();
    }

    public static class Serializer extends DimensionConditionSerializer<TimelinesDimensionCondition> {

        @Override
        public MapCodec<TimelinesDimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DimensionCondition, TimelinesDimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Timelines")
                    .setDescription("Checks if the current dimension contains any of the specified timelines.")
                    .add("timelines", KryptoniteDocumented.TYPE_TIMELINE_HOLDER_SET, "Timeline IDs or tags that must exist for the current dimension.")
                    .addExampleObject(new TimelinesDimensionCondition(provider.lookupOrThrow(Registries.TIMELINE).getOrThrow(TimelineTags.UNIVERSAL)))
                    .addExampleObject(new TimelinesDimensionCondition(provider.lookupOrThrow(Registries.TIMELINE).getOrThrow(TimelineTags.IN_OVERWORLD)))
                    .addExampleObject(new TimelinesDimensionCondition(provider.lookupOrThrow(Registries.TIMELINE).getOrThrow(TimelineTags.IN_NETHER)))
                    .addExampleObject(new TimelinesDimensionCondition(provider.lookupOrThrow(Registries.TIMELINE).getOrThrow(TimelineTags.IN_END)));
        }
    }

}