package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

public record UUIDCondition(Value uuid) implements Condition {

    public static final MapCodec<UUIDCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.fieldOf("uuid").forGetter(UUIDCondition::uuid)
    ).apply(instance, UUIDCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, UUIDCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), UUIDCondition::uuid,
            UUIDCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        return entity.getUUID().toString().equals(uuid.getAsString(context));
    }

    @Override
    public ConditionSerializer<UUIDCondition> getSerializer() {
        return KryptoniteConditionSerializers.UUID.get();
    }

    public static class Serializer extends ConditionSerializer<UUIDCondition> {

        @Override
        public MapCodec<UUIDCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, UUIDCondition> builder, HolderLookup.Provider provider) {
            builder.setName("UUID")
                    .setDescription("Checks if the entity has the specified UUID.")
                    .add("uuid", TYPE_STRING_VALUE, "The uuid to check as a string value.")
                    .addExampleObject(new UUIDCondition(new StaticValue("8667ba71-b85a-4004-af54-457a9734eed7")));
        }
    }

}