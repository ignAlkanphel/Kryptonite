package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.util.apoli.MiscUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public class HasRespawnCondition implements Condition {

    public static final HasRespawnCondition INSTANCE = new HasRespawnCondition();

    public static final MapCodec<HasRespawnCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, HasRespawnCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean test(DataContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;

        return MiscUtil.hasLocalVanillaRespawnPosition(player);
    }

    @Override
    public ConditionSerializer<HasRespawnCondition> getSerializer() {
        return KryptoniteConditionSerializers.HAS_RESPAWN.get();
    }

    public static class Serializer extends ConditionSerializer<HasRespawnCondition> {

        @Override
        public MapCodec<HasRespawnCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, HasRespawnCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Has Respawn")
                    .setDescription("Checks if the (server) player has a respawn point set (e.g. bed/respawn anchor).")
                    .addExampleObject(INSTANCE);
        }
    }

}