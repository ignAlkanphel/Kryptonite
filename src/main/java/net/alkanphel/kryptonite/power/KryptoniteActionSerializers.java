package net.alkanphel.kryptonite.power;

import net.alkanphel.kryptonite.Kryptonite;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public class KryptoniteActionSerializers {
    public static final DeferredRegister<ActionSerializer<?>> ACTION_SERIALIZERS = DeferredRegister.create(PalladiumRegistryKeys.ACTION_SERIALIZER, Kryptonite.MOD_ID);

}