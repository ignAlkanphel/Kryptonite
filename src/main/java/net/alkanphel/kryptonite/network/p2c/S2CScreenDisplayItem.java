package net.alkanphel.kryptonite.network.p2c;

import net.alkanphel.kryptonite.Kryptonite;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2CScreenDisplayItem(ItemStack stack) implements CustomPacketPayload {

    public static final Type<S2CScreenDisplayItem> TYPE = new Type<>(Kryptonite.id("s2c/screen_display_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CScreenDisplayItem> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, S2CScreenDisplayItem::stack,
            S2CScreenDisplayItem::new
    );

    @Override
    public Type<S2CScreenDisplayItem> type() {
        return TYPE;
    }

    public static void handle(S2CScreenDisplayItem packet, IPayloadContext context) {
        context.enqueueWork(() -> Kryptonite.PROXY.screenDisplayItem(packet.stack()));
    }

}