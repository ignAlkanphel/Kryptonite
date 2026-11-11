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

import java.util.List;

public record DimensionWrapperCondition(List<DimensionCondition> dimensionConditions) implements Condition {

    public static final MapCodec<DimensionWrapperCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DimensionCondition.LIST_CODEC.optionalFieldOf("dimension_conditions", List.of()).forGetter(DimensionWrapperCondition::dimensionConditions)
    ).apply(instance, DimensionWrapperCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DimensionWrapperCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(DimensionCondition.LIST_CODEC),
            DimensionWrapperCondition::dimensionConditions,
            DimensionWrapperCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var level = context.getLevel();
        var entity = context.getEntity();
        if (level == null || entity == null) return false;

        return DimensionCondition.checkConditions(dimensionConditions, level.dimensionType(), level, entity);
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
                    .setDescription("Acts as a bridge between the (normal) base Palladium conditions and dimension conditions.")
                    .addOptional("dimension_conditions", KryptoniteDocumented.TYPE_DIMENSION_CONDITION_LIST, "Dimension conditions to check. Returns true if none specified.");
        }
    }

}