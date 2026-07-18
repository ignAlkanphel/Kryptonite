package net.alkanphel.kryptonite.power;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.action.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public class KryptoniteActionSerializers {

    public static final DeferredRegister<ActionSerializer<?>> ACTION_SERIALIZERS = DeferredRegister.create(PalladiumRegistryKeys.ACTION_SERIALIZER, Kryptonite.MOD_ID);

    public static final DeferredHolder<ActionSerializer<?>, AreaOfEffectAction.Serializer> AREA_OF_EFFECT = ACTION_SERIALIZERS.register("area_of_effect", AreaOfEffectAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, DisplayItemActivationAction.Serializer> DISPLAY_ITEM_ACTIVATION = ACTION_SERIALIZERS.register("display_item_activation", DisplayItemActivationAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, ExplodeAction.Serializer> EXPLODE = ACTION_SERIALIZERS.register("explode", ExplodeAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, ExtinguishAction.Serializer> EXTINGUISH = ACTION_SERIALIZERS.register("extinguish", ExtinguishAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, SetFallDistanceAction.Serializer> SET_FALL_DISTANCE = ACTION_SERIALIZERS.register("set_fall_distance", SetFallDistanceAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, SetOnFireAction.Serializer> SET_ON_FIRE = ACTION_SERIALIZERS.register("set_on_fire", SetOnFireAction.Serializer::new);

}