package net.alkanphel.kryptonite.power;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.condition.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public class KryptoniteConditionSerializers {

    public static final DeferredRegister<ConditionSerializer<?>> CONDITIONS_SERIALIZERS = DeferredRegister.create(PalladiumRegistryKeys.CONDITION_SERIALIZER, Kryptonite.MOD_ID);

    public static final DeferredHolder<ConditionSerializer<?>, DimensionWrapperCondition.Serializer> DIMENSION = CONDITIONS_SERIALIZERS.register("dimension", DimensionWrapperCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, KeyBindCondition.Serializer> KEY_BIND = CONDITIONS_SERIALIZERS.register("key_bind", KeyBindCondition.Serializer::new);

}