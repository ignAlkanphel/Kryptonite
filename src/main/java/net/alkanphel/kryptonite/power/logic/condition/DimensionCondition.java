package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.DimensionConditionContext;
import net.alkanphel.kryptonite.power.logic.filter.DimensionFilter;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public record DimensionCondition(DimensionFilter dimensionFilter) implements Condition {

    public static final MapCodec<DimensionCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DimensionFilter.CODEC.optionalFieldOf("dimension_filters", DimensionFilter.EMPTY).forGetter(DimensionCondition::dimensionFilter)
    ).apply(instance, DimensionCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DimensionCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(DimensionFilter.CODEC),
            DimensionCondition::dimensionFilter,
            DimensionCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var level = context.getLevel();
        if (level == null) return false;

        return dimensionFilter.test(new DimensionConditionContext(level.dimensionType(), level, context.getEntity()));
    }

    @Override
    public ConditionSerializer<DimensionCondition> getSerializer() {
        return KryptoniteConditionSerializers.DIMENSION.get();
    }

    public static class Serializer extends ConditionSerializer<DimensionCondition> {

        @Override
        public MapCodec<DimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, DimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Dimension")
                    .setDescription("Tests and filters for the current dimension.")
                    .addOptional("dimension_filters", DimensionFilter.documentation(provider), "Damage filter(s) that must pass. Returns true if none specified.");
        }
    }

}