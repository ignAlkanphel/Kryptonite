package net.alkanphel.kryptonite;

import com.mojang.logging.LogUtils;
import net.alkanphel.kryptonite.network.KryptoniteNetwork;
import net.alkanphel.kryptonite.power.*;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiActionSerializers;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializers;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializers;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializers;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageConditionSerializers;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializers;
import net.alkanphel.kryptonite.proxy.KryptoniteProxy;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Kryptonite.MOD_ID)
@EventBusSubscriber(modid = Kryptonite.MOD_ID)
public class Kryptonite {
    public static final String MOD_ID = "kryptonite";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static KryptoniteProxy PROXY = new KryptoniteProxy();

    public Kryptonite(IEventBus modEventBus, ModContainer modContainer) {
        KryptoniteActionSerializers.ACTION_SERIALIZERS.register(modEventBus);
        KryptoniteAbilitySerializers.ABILITIES_SERIALIZERS.register(modEventBus);
        KryptoniteConditionSerializers.CONDITIONS_SERIALIZERS.register(modEventBus);
        BiActionSerializers.BI_ACTION_SERIALIZERS.register(modEventBus);
        BlockActionSerializers.BLOCK_ACTION_SERIALIZERS.register(modEventBus);
        BiConditionSerializers.BI_CONDITION_SERIALIZERS.register(modEventBus);
        BlockConditionSerializers.BLOCK_CONDITION_SERIALIZERS.register(modEventBus);
        DimensionConditionSerializers.DIMENSION_CONDITION_SERIALIZERS.register(modEventBus);
        DamageConditionSerializers.DAMAGE_CONDITION_SERIALIZERS.register(modEventBus);

        KryptoniteNetwork.init();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}