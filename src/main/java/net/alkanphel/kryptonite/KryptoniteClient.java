package net.alkanphel.kryptonite;

import net.alkanphel.kryptonite.client.datagen.KryptoniteLangProvider;
import net.alkanphel.kryptonite.power.compat.lambdynlights.DynLightsCompatClient;
import net.alkanphel.kryptonite.proxy.KryptoniteProxyClient;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

@Mod(value = Kryptonite.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Kryptonite.MOD_ID, value = Dist.CLIENT)
public class KryptoniteClient {

    public KryptoniteClient(ModContainer container) {
        Kryptonite.PROXY = new KryptoniteProxyClient();

        if (ModList.get().isLoaded("lambdynlights")) {
            DynLightsCompatClient.init();
        }

    }

    @SubscribeEvent
    static void onGatherDataClient(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        event.createProvider(KryptoniteLangProvider.English::new);
    }

    @SubscribeEvent
    static void onGenerateDocumentation(LevelEvent.Load e) {
        LevelAccessor clientLevel = e.getLevel();
        CodecDocumentationBuilder.startListening();
        CodecDocumentationBuilder.createDocFiles();
    }

}