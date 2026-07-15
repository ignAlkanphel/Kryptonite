package net.alkanphel.kryptonite.power.logic.action.bi.internal;

import com.mojang.serialization.Codec;
import net.alkanphel.kryptonite.power.logic.context.BiActionContext;
import net.alkanphel.kryptonite.registry.KryptoniteRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;

import java.util.List;

public abstract class BiAction {

    public static final Codec<BiAction> CODEC = KryptoniteRegistries.BI_ACTION_SERIALIZER.byNameCodec().dispatch(BiAction::getSerializer, BiActionSerializer::codec);
    public static final Codec<List<BiAction>> LIST_CODEC = ExtraCodecs.compactListCodec(CODEC);

    public abstract boolean run(BiActionContext context);

    public static boolean runList(List<BiAction> biActions, Entity actor, Entity target) {
        return runList(biActions, new BiActionContext(actor, target));
    }

    public static boolean runList(List<BiAction> biActions, BiActionContext context) {
        boolean result = false;

        for (BiAction biAction : biActions) {
            result |= biAction.run(context);
        }

        return result;
    }

    public abstract BiActionSerializer<?> getSerializer();

}