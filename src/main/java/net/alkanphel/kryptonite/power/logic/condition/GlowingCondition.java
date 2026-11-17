package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public class GlowingCondition implements Condition {

    public static final GlowingCondition INSTANCE = new GlowingCondition();

    public static final MapCodec<GlowingCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, GlowingCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        return !entity.level().isClientSide()
                ? entity.isCurrentlyGlowing()
                : Minecraft.getInstance().shouldEntityAppearGlowing(entity);
    }

    @Override
    public ConditionSerializer<GlowingCondition> getSerializer() {
        return KryptoniteConditionSerializers.GLOWING.get();
    }

    public static class Serializer extends ConditionSerializer<GlowingCondition> {

        @Override
        public MapCodec<GlowingCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, GlowingCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Glowing")
                    .setDescription("Checks if the entity has a glowing outline (e.g. the \"minecraft:glowing\" effect).")
                    .addExampleObject(INSTANCE);
        }
    }

}