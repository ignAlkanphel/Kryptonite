package net.alkanphel.kryptonite.registry;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializer;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

@EventBusSubscriber(modid = Kryptonite.MOD_ID)
public class KryptoniteRegistries {

    public static final Registry<DimensionConditionSerializer<?>> DIMENSION_CONDITION_SERIALIZER = new RegistryBuilder<>(KryptoniteRegistryKeys.DIMENSION_CONDITION_SERIALIZER).create();

    @SubscribeEvent
    static void registerRegistries(NewRegistryEvent e) {
        e.register(DIMENSION_CONDITION_SERIALIZER);
    }

}