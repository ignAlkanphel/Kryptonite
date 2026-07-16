package net.alkanphel.kryptonite.power.logic.action.bi.internal;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.action.bi.SetInLoveBiAction;
import net.alkanphel.kryptonite.power.logic.action.bi.TameBiAction;
import net.alkanphel.kryptonite.power.logic.action.bi.meta.*;
import net.alkanphel.kryptonite.registry.KryptoniteRegistryKeys;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BiActionSerializers {

    public static final DeferredRegister<BiActionSerializer<?>> BI_ACTION_SERIALIZERS = DeferredRegister.create(KryptoniteRegistryKeys.BI_ACTION_SERIALIZER, Kryptonite.MOD_ID);

    public static final DeferredHolder<BiActionSerializer<?>, ActorActionBiAction.Serializer> ACTOR_ACTION = BI_ACTION_SERIALIZERS.register("actor_action", ActorActionBiAction.Serializer::new);
    public static final DeferredHolder<BiActionSerializer<?>, InvertBiAction.Serializer> INVERT = BI_ACTION_SERIALIZERS.register("invert", InvertBiAction.Serializer::new);
    public static final DeferredHolder<BiActionSerializer<?>, TargetActionBiAction.Serializer> TARGET_ACTION = BI_ACTION_SERIALIZERS.register("target_action", TargetActionBiAction.Serializer::new);

    public static final DeferredHolder<BiActionSerializer<?>, SetInLoveBiAction.Serializer> SET_IN_LOVE = BI_ACTION_SERIALIZERS.register("set_in_love", SetInLoveBiAction.Serializer::new);
    public static final DeferredHolder<BiActionSerializer<?>, TameBiAction.Serializer> TAME = BI_ACTION_SERIALIZERS.register("tame", TameBiAction.Serializer::new);

}