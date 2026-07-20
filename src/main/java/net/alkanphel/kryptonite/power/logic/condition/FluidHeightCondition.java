package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;

public record FluidHeightCondition(TagKey<Fluid> fluidTag, NumberComparator comparator, Value compareTo) implements Condition {

    public static final MapCodec<FluidHeightCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TagKey.hashedCodec(Registries.FLUID).fieldOf("fluid_tag").forGetter(FluidHeightCondition::fluidTag),
            NumberComparator.CODEC.fieldOf("comparator").forGetter(FluidHeightCondition::comparator),
            Value.CODEC.fieldOf("compare_to").forGetter(FluidHeightCondition::compareTo)
    ).apply(instance, FluidHeightCondition::new));

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        return comparator.compare(entity.getFluidHeight(fluidTag), compareTo.getAsDouble(context));
    }

    @Override
    public ConditionSerializer<FluidHeightCondition> getSerializer() {
        return KryptoniteConditionSerializers.FLUID_HEIGHT.get();
    }

    public static class Serializer extends ConditionSerializer<FluidHeightCondition> {

        @Override
        public MapCodec<FluidHeightCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, FluidHeightCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Fluid Height")
                    .setDescription("Checks how high specific fluid is at the entity. A fluid height of 0 means the entity is not touching fluid.")
                    .addOptional("fluid_tag", TYPE_FLUID_TAG, "The fluid tags to check.")
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator being used")
                    .add("compare_to", TYPE_VALUE, "The value that is being compared against")
                    .addExampleObject(new FluidHeightCondition(FluidTags.WATER, NumberComparator.GREATER_THAN, new StaticValue(0.5)));
        }
    }

}