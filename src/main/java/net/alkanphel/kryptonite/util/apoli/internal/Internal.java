package net.alkanphel.kryptonite.util.apoli.internal;

import net.alkanphel.kryptonite.util.apoli.access.EntityLinkedType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;

/**
 * @author Ampflower
 **/
@ApiStatus.Internal
public final class Internal {

    /**
     * Cleans up the global state of any stray world references when called.
     * <p>
     * Note: Some entries cleaned use ThreadLocal and may not need to be cleaned by the server,
     * as the server discards its thread when it exists.
     * */
    public static void globalStateCleanup(RegistryAccess registryAccess) {
        for (final EntityType<?> entityType : registryAccess.lookupOrThrow(Registries.ENTITY_TYPE)) {
            if (entityType instanceof EntityLinkedType entityLinkedType) {
                entityLinkedType.kryptonite$setEntity(null);
            }
        }
    }

}