package net.alkanphel.kryptonite.util.apoli.keybind;

import net.alkanphel.kryptonite.Kryptonite;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Kryptonite.MOD_ID)
public class KeyBindActivityCleanup {

    @SubscribeEvent
    public static void onServerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            KeyBindActivityManager.remove(player);
        }
    }

    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        KeyBindActivityManager.tickAll();
    }

}