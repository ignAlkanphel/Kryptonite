package net.alkanphel.kryptonite.registry;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemActionSerializer;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

@EventBusSubscriber(modid = Kryptonite.MOD_ID)
public class KryptoniteRegistries {

    public static final Registry<BlockActionSerializer<?>> BLOCK_ACTION_SERIALIZER = new RegistryBuilder<>(KryptoniteRegistryKeys.BLOCK_ACTION_SERIALIZER).create();
    public static final Registry<ItemActionSerializer<?>> ITEM_ACTION_SERIALIZER = new RegistryBuilder<>(KryptoniteRegistryKeys.ITEM_ACTION_SERIALIZER).create();

    @SubscribeEvent
    static void registerRegistries(NewRegistryEvent e) {
        e.register(BLOCK_ACTION_SERIALIZER);
        e.register(ITEM_ACTION_SERIALIZER);
    }

}