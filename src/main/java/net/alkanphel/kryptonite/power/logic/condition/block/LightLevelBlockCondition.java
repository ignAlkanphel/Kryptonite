package net.alkanphel.kryptonite.power.logic.condition.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record LightLevelBlockCondition(Optional<LightType> lightType, NumberComparator comparator, Value compareTo) implements BlockCondition {

    public static final MapCodec<LightLevelBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LightType.CODEC.optionalFieldOf("light_type").forGetter(LightLevelBlockCondition::lightType),
            NumberComparator.CODEC.fieldOf("comparator").forGetter(LightLevelBlockCondition::comparator),
            Value.CODEC.fieldOf("compare_to").forGetter(LightLevelBlockCondition::compareTo)
    ).apply(instance, LightLevelBlockCondition::new));

    @Override
    public boolean test(BlockConditionContext context) {
        Level level = context.level();
        BlockPos pos = context.pos();

        int lightLevel = lightType.map(type -> switch (type) {
            case SKY -> level.getBrightness(LightLayer.SKY, pos);
            case BLOCK -> level.getBrightness(LightLayer.BLOCK, pos);
        }).orElseGet(() -> level.getLightEngine().getRawBrightness(pos, level.getSkyDarken()));

        int comparedValue = compareTo.getAsInt(null);

        return comparator.compare(lightLevel, comparedValue);
    }

    @Override
    public BlockConditionSerializer<LightLevelBlockCondition> getSerializer() {
        return BlockConditionSerializers.LIGHT_LEVEL.get();
    }

    public static class Serializer extends BlockConditionSerializer<LightLevelBlockCondition> {

        @Override
        public MapCodec<LightLevelBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, LightLevelBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Light Level")
                    .setDescription("Allows checking the light level at the block's position.")
                    .addOptional("light_type", SettingType.enumList(LightType.values()), "If specified, determines the type of light level to compare.")
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator being used")
                    .add("compare_to", TYPE_INT, "The value that is being compared against")
                    .addExampleObject(new LightLevelBlockCondition(Optional.of(LightType.BLOCK), NumberComparator.GREATER_THAN, new StaticValue(10)));
        }
    }

    public enum LightType implements StringRepresentable {
        SKY("sky"),
        BLOCK("block");

        public static final Codec<LightType> CODEC = StringRepresentable.fromEnum(LightType::values);
        private final String name;

        LightType(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

}