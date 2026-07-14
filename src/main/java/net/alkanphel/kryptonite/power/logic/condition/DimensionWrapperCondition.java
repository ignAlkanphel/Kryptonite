package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;

import java.util.Optional;

public record DimensionWrapperCondition(Optional<DimensionCondition> dimensionCondition) implements Condition {

    public static final MapCodec<DimensionWrapperCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DimensionCondition.CODEC.optionalFieldOf("dimension_conditions").forGetter(DimensionWrapperCondition::dimensionCondition)
    ).apply(instance, DimensionWrapperCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DimensionWrapperCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.fromCodecWithRegistriesTrusted(DimensionCondition.CODEC)),
            DimensionWrapperCondition::dimensionCondition,
            DimensionWrapperCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var level = context.getLevel();
        if (level == null || context.getEntity() == null) return false;

        return dimensionCondition.map(ctx -> ctx.test(level.dimensionType(), level, context.getEntity())).orElse(true);
    }

    @Override
    public ConditionSerializer<DimensionWrapperCondition> getSerializer() {
        return KryptoniteConditionSerializers.DIMENSION.get();
    }

    public static class Serializer extends ConditionSerializer<DimensionWrapperCondition> {

        @Override
        public MapCodec<DimensionWrapperCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, DimensionWrapperCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Dimension")
                    .setDescription("Acts as a bridge between (normal) base Palladium conditions and dimension conditions.")
                    .addOptional("dimension_conditions", KryptoniteDocumented.TYPE_DIMENSION_CONDITION_LIST, "Dimension conditions to check. Returns true if none specified.");
        }
    }

}