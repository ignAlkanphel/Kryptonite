package net.alkanphel.kryptonite.util.apoli.keybind;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class KeyBindActivityManager {

    private static final ConcurrentMap<UUID, KeyBindActivity> ACTIVITY = new ConcurrentHashMap<>();

    public static KeyBindActivity getOrCreate(ServerPlayer player) {
        return ACTIVITY.computeIfAbsent(player.getUUID(), _ -> new KeyBindActivity());
    }

    public static KeyBindActivity get(Entity entity) {
        return ACTIVITY.get(entity.getUUID());
    }

    public static void remove(ServerPlayer player) {
        ACTIVITY.remove(player.getUUID());
    }

    public static void tickAll() {
        for (KeyBindActivity activity : ACTIVITY.values()) {
            activity.tick();
        }
    }

}