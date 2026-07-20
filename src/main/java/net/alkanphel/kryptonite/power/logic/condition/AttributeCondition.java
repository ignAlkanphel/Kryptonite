package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;

import java.util.Optional;

public record AttributeCondition(Holder<Attribute> attribute, NumberComparator comparator, Value compareTo) implements Condition {

    public static final MapCodec<AttributeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Attribute.CODEC.fieldOf("attribute").forGetter(AttributeCondition::attribute),
            NumberComparator.CODEC.fieldOf("comparator").forGetter(AttributeCondition::comparator),
            Value.CODEC.fieldOf("compare_to").forGetter(AttributeCondition::compareTo)
    ).apply(instance, AttributeCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AttributeCondition> STREAM_CODEC = StreamCodec.composite(
            Attribute.STREAM_CODEC, AttributeCondition::attribute,
            NumberComparator.STREAM_CODEC, AttributeCondition::comparator,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), AttributeCondition::compareTo,
            AttributeCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        if (context.getEntity() instanceof LivingEntity livingEntity) {
            return Optional.ofNullable(livingEntity.getAttribute(attribute))
                    .map(AttributeInstance::getValue)
                    .map(value -> comparator.compare(value, compareTo.getAsDouble(context)))
                    .orElse(false);
        }

        else {
            return false;
        }

    }

    @Override
    public ConditionSerializer<?> getSerializer() {
        return KryptoniteConditionSerializers.ATTRIBUTE.get();
    }

    public static class Serializer extends ConditionSerializer<AttributeCondition> {

        @Override
        public MapCodec<AttributeCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, AttributeCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Attribute")
                    .setDescription("Checks if the attribute value of the entity matches the comparison.")
                    .add("attribute", TYPE_ATTRIBUTE, "The attribute to check.")
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator being used")
                    .add("compare_to", TYPE_VALUE, "The value that is being compared against")
                    .addExampleObject(new AttributeCondition(Attributes.ARMOR, NumberComparator.GREATER_OR_EQUAL, new StaticValue(10.0D)));
        }
    }

}