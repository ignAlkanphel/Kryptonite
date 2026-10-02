package net.alkanphel.kryptonite.power.attribute;

import net.alkanphel.kryptonite.Kryptonite;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class KryptoniteAttributes {
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, Kryptonite.MOD_ID);

    public static final DeferredHolder<Attribute, Attribute> MODEL_DEPTH = ATTRIBUTES.register("model_depth", () -> new RangedAttribute("attribute.kryptonite.name.model_width", 1.0, 0.0625, 16.0).setSyncable(true).setSentiment(Attribute.Sentiment.NEUTRAL));

    public static void register(IEventBus bus) {
        ATTRIBUTES.register(bus);
    }

}