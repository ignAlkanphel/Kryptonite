package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.util.apoli.MiscUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public class RespawnResetAction extends Action {

    public static final MapCodec<RespawnResetAction> CODEC = MapCodec.unit(new RespawnResetAction());
    public static final StreamCodec<RegistryFriendlyByteBuf, RespawnResetAction> STREAM_CODEC = StreamCodec.unit(new RespawnResetAction());

    public RespawnResetAction() {}

    @Override
    public boolean run(DataContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;

        MiscUtil.clearLocalVanillaRespawnPosition(player, false);
        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.RESPAWN_RESET.get();
    }

    public static class Serializer extends ActionSerializer<RespawnResetAction> {

        @Override
        public MapCodec<RespawnResetAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, RespawnResetAction> builder, HolderLookup.Provider provider) {
            builder.setName("Respawn Reset")
                    .setDescription("Resets the player's \"local\" spawn (e.g. bed/respawn anchor).")
                    .addExampleObject(new RespawnResetAction());

        }
    }

}