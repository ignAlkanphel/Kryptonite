package net.alkanphel.kryptonite.util;

import net.alkanphel.kryptonite.Kryptonite;
import net.minecraft.tags.TagKey;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public class KryptoniteTags {

    public interface Powers {
        TagKey<Power> IGNORES_MAX_SUPERPOWER_SETS = create("ignores_max_superpower_sets");
        TagKey<Power> IGNORES_SPECTATOR_CHECK = create("ignores_spectator_check");
        TagKey<Power> IGNORES_DAMPENING = create("ignores_dampening");

        private static TagKey<Power> create(String name) {
            return TagKey.create(PalladiumRegistryKeys.POWER, Kryptonite.id(name));
        }
    }

}