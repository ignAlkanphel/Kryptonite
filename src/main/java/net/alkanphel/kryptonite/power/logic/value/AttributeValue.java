package net.alkanphel.kryptonite.power.logic.value;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteValueSerializers;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.FloatValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.logic.value.ValueSerializer;

import java.util.Optional;

public class AttributeValue extends FloatValue {

    public static final MapCodec<AttributeValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Attribute.CODEC.fieldOf("attribute").forGetter(AttributeValue::attribute),
            modifyFunctionCodec()
    ).apply(instance, AttributeValue::new));

    private final Holder<Attribute> attribute;

    public AttributeValue(Holder<Attribute> attribute, String molang) {
        super(molang);
        this.attribute = attribute;
    }

    public Holder<Attribute> attribute() {
        return this.attribute;
    }

    @Override
    public float getFloat(DataContext context) {
        if (context.getEntity() instanceof LivingEntity livingEntity) {
            return Optional.ofNullable(livingEntity.getAttribute(attribute))
                    .map(AttributeInstance::getValue)
                    .orElse(0.0D)
                    .floatValue();
        }

        return 0F;
    }

    @Override
    public ValueSerializer<?> getSerializer() {
        return KryptoniteValueSerializers.ATTRIBUTE.get();
    }

    public static class Serializer extends FloatSerializer<AttributeValue> {

        @Override
        public MapCodec<AttributeValue> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Value, AttributeValue> builder, HolderLookup.Provider provider) {
            builder.setName("Attribute")
                    .setDescription("Returns the attribute value of the entity.")
                    .add("attribute", TYPE_ATTRIBUTE, "The id of the attribute to return.")
                    .addExampleObject(new AttributeValue(Attributes.ARMOR, "this * 2"));
        }
    }

}