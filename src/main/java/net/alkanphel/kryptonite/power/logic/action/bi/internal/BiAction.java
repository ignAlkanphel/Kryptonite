package net.alkanphel.kryptonite.power.logic.action.bi.internal;

import com.mojang.serialization.Codec;
import net.alkanphel.kryptonite.power.logic.BiLogicEnvironment;
import net.alkanphel.kryptonite.power.logic.context.BiContext;
import net.alkanphel.kryptonite.registry.KryptoniteRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;

import java.util.List;

public abstract class BiAction {

    public static final Codec<BiAction> CODEC = KryptoniteRegistries.BI_ACTION_SERIALIZER.byNameCodec().dispatch(BiAction::getSerializer, BiActionSerializer::codec);
    public static final Codec<List<BiAction>> LIST_CODEC = ExtraCodecs.compactListCodec(CODEC);

    public final boolean run(BiContext context) {
        return this.getEnvironment().matches(context) && this.execute(context);
    }

    public BiLogicEnvironment getEnvironment() {
        return this.getSerializer().getEnvironment();
    }

    public static BiLogicEnvironment getEnvironment(List<BiAction> biActions) {
        BiLogicEnvironment environment = null;

        for (BiAction biAction : biActions) {
            var actionEnvironment = biAction.getEnvironment();

            if (environment == null) {
                environment = actionEnvironment;
            } else if (environment != actionEnvironment) {
                return BiLogicEnvironment.BOTH;
            }
        }

        return environment != null ? environment : BiLogicEnvironment.SERVER;
    }

    protected abstract boolean execute(BiContext context);

    public static boolean runList(List<BiAction> biActions, Entity actor, Entity target) {
        return runList(biActions, new BiContext(actor, target));
    }

    public static boolean runList(List<BiAction> biActions, BiContext context) {
        boolean result = false;

        for (BiAction biAction : biActions) {
            result |= biAction.run(context);
        }

        return result;
    }

    public abstract BiActionSerializer<?> getSerializer();

}