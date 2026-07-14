package net.alkanphel.kryptonite.util;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

public final class AttachmentUtil {
    private AttachmentUtil() {}

    public static <T> @Nullable T get(Entity entity, Identifier id) {
        AttachmentType<?> type = NeoForgeRegistries.ATTACHMENT_TYPES.getValue(id);
        if (type == null) return null;

        return (T) entity.getData(type);
    }

    public static <T> void set(Entity entity, Identifier id, T value) {
        AttachmentType<?> raw = NeoForgeRegistries.ATTACHMENT_TYPES.getValue(id);
        if (raw == null) return;

        AttachmentType<T> type = (AttachmentType<T>) raw;

        entity.setData(type, value);
    }

    public static String getString(Entity entity, Identifier id, String fallback) {
        Object value = get(entity, id);
        return value instanceof String s ? s : fallback;
    }

    public static @Nullable String getString(Entity entity, Identifier id) {
        Object value = get(entity, id);
        return value instanceof String s ? s : null;
    }

    public static boolean getBoolean(Entity entity, Identifier id) {
        return get(entity, id) instanceof Boolean b && b;
    }

    public static int getInt(Entity entity, Identifier id, int fallback) {
        return get(entity, id) instanceof Integer i ? i : fallback;
    }

    public static long getLong(Entity entity, Identifier id, long fallback) {
        return get(entity, id) instanceof Long l ? l : fallback;
    }

    public static float getFloat(Entity entity, Identifier id, float fallback) {
        return get(entity, id) instanceof Float f ? f : fallback;
    }

    public static double getDouble(Entity entity, Identifier id, double fallback) {
        return get(entity, id) instanceof Double d ? d : fallback;
    }

}