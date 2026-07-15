package net.alkanphel.kryptonite.power.logic.action.item.internal;

import com.mojang.serialization.Codec;
import net.alkanphel.kryptonite.power.logic.context.ItemActionContext;
import net.alkanphel.kryptonite.registry.KryptoniteRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.level.Level;

import java.util.List;

public abstract class ItemAction {

    public static final Codec<ItemAction> CODEC = KryptoniteRegistries.ITEM_ACTION_SERIALIZER.byNameCodec().dispatch(ItemAction::getSerializer, ItemActionSerializer::codec);
    public static final Codec<List<ItemAction>> LIST_CODEC = ExtraCodecs.compactListCodec(CODEC);

    protected abstract boolean run(ItemActionContext context);

    public boolean run(Level level, SlotAccess slotAccess) {
        if (level instanceof ServerLevel serverLevel) {
            return run(new ItemActionContext(serverLevel, slotAccess));
        }
        return false;
    }

    public static boolean runList(List<ItemAction> actions, Level level, SlotAccess slotAccess) {
        boolean result = false;

        if (level instanceof ServerLevel serverLevel) {
            ItemActionContext context = new ItemActionContext(serverLevel, slotAccess);
            if (context.slotAccess() == null || context.slotAccess().get().isEmpty()) return false;

            for (ItemAction action : actions) {
                result |= action.run(context);
            }
        }

        return result;
    }

    // original
    public static boolean runList(List<ItemAction> actions, ItemActionContext context) {
        if (context.slotAccess() == null || context.slotAccess().get().isEmpty()) return false;
        boolean result = false;

        for (ItemAction action : actions) {
            result |= action.run(context);
        }

        return result;
    }

    public abstract ItemActionSerializer<?> getSerializer();

}