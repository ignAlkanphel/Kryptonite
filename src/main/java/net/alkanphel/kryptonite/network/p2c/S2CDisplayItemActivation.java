package net.alkanphel.kryptonite.network.p2c;

import net.alkanphel.kryptonite.Kryptonite;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2CDisplayItemActivation(ItemStack stack) implements CustomPacketPayload {

    public static final Type<S2CDisplayItemActivation> TYPE = new Type<>(Kryptonite.id("s2c/display_item_activation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CDisplayItemActivation> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, S2CDisplayItemActivation::stack,
            S2CDisplayItemActivation::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CDisplayItemActivation packet, IPayloadContext context) {
        context.enqueueWork(() -> Kryptonite.PROXY.displayItemActivation(packet.stack()));
    }

}