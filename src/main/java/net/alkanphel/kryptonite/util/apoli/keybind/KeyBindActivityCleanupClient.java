package net.alkanphel.kryptonite.util.apoli.keybind;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.mixin.client.KeyMappingAccessor;
import net.alkanphel.kryptonite.network.p2s.C2SKeyBindActivity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = Kryptonite.MOD_ID, value = Dist.CLIENT)
public class KeyBindActivityCleanupClient {

    private static Set<String> lastPressedKeys = new HashSet<>();

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        lastPressedKeys = new HashSet<>();
    }

    @SubscribeEvent
    static void onClientTickPost(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Set<String> currentPressed = new HashSet<>();
        for (Map.Entry<String, KeyMapping> entry : KeyMappingAccessor.kryptonite$getKeysById().entrySet()) {
            if (entry.getValue().isDown()) {
                currentPressed.add(entry.getKey());
            }
        }

        if (!currentPressed.equals(lastPressedKeys)) {
            ClientPacketDistributor.sendToServer(new C2SKeyBindActivity(currentPressed));
            lastPressedKeys = currentPressed;
        }
    }

}