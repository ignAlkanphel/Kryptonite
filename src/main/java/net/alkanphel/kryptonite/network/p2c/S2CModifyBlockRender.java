package net.alkanphel.kryptonite.network.p2c;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.ability.ModifyBlockRenderAbility;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2CModifyBlockRender(ModifyBlockRenderAbility.Mode mode) implements CustomPacketPayload {

    public static final Type<S2CModifyBlockRender> TYPE = new Type<>(Kryptonite.id("s2c/modify_block_render"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CModifyBlockRender> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(ModifyBlockRenderAbility.Mode.class), S2CModifyBlockRender::mode,
            S2CModifyBlockRender::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CModifyBlockRender packet, IPayloadContext context) {
        context.enqueueWork(() -> Kryptonite.PROXY.modifyBlockRender(packet.mode()));
    }

}