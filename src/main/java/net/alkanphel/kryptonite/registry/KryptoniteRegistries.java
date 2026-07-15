package net.alkanphel.kryptonite.registry;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializer;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

@EventBusSubscriber(modid = Kryptonite.MOD_ID)
public class KryptoniteRegistries {

    public static final Registry<BlockActionSerializer<?>> BLOCK_ACTION_SERIALIZER = new RegistryBuilder<>(KryptoniteRegistryKeys.BLOCK_ACTION_SERIALIZER).create();
    public static final Registry<BlockConditionSerializer<?>> BLOCK_CONDITION_SERIALIZER = new RegistryBuilder<>(KryptoniteRegistryKeys.BLOCK_CONDITION_SERIALIZER).create();
    public static final Registry<DimensionConditionSerializer<?>> DIMENSION_CONDITION_SERIALIZER = new RegistryBuilder<>(KryptoniteRegistryKeys.DIMENSION_CONDITION_SERIALIZER).create();
    public static final Registry<DamageConditionSerializer<?>> DAMAGE_CONDITION_SERIALIZER = new RegistryBuilder<>(KryptoniteRegistryKeys.DAMAGE_CONDITION_SERIALIZER).create();

    @SubscribeEvent
    static void registerRegistries(NewRegistryEvent e) {
        e.register(BLOCK_ACTION_SERIALIZER);
        e.register(BLOCK_CONDITION_SERIALIZER);
        e.register(DIMENSION_CONDITION_SERIALIZER);
        e.register(DAMAGE_CONDITION_SERIALIZER);
    }

}