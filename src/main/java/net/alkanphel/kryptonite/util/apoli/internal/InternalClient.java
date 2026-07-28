package net.alkanphel.kryptonite.util.apoli.internal;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;

/**
 * @author Ampflower
 **/
@ApiStatus.Internal
public final class InternalClient {

    public static void onClientWorldChanged() {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        Internal.globalStateCleanup(level.registryAccess());
    }

}