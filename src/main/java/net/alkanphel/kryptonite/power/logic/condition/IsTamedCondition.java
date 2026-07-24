package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.TamableAnimal;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public class IsTamedCondition implements Condition {

    public static final IsTamedCondition INSTANCE = new IsTamedCondition();

    public static final MapCodec<IsTamedCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, IsTamedCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        return context.getEntity() instanceof TamableAnimal tameable && tameable.getOwner() != null;
    }

    @Override
    public ConditionSerializer<IsTamedCondition> getSerializer() {
        return KryptoniteConditionSerializers.IS_TAMED.get();
    }

    public static class Serializer extends ConditionSerializer<IsTamedCondition> {

        @Override
        public MapCodec<IsTamedCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, IsTamedCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Is Tamed")
                    .setDescription("Checks if the entity is tamed.")
                    .addExampleObject(INSTANCE);
        }
    }

}